package tech.wetech.flexmodel.domain.model.flow.processor;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.codegen.entity.FlowInstance;
import tech.wetech.flexmodel.codegen.entity.FlowInstanceMapping;
import tech.wetech.flexmodel.codegen.entity.NodeInstance;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.ElementInstance;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.FlowInfo;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.FlowInstanceBO;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import tech.wetech.flexmodel.domain.model.flow.dto.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.dto.param.CommitTaskParam;
import tech.wetech.flexmodel.domain.model.flow.dto.param.RollbackTaskParam;
import tech.wetech.flexmodel.domain.model.flow.dto.param.StartProcessParam;
import tech.wetech.flexmodel.domain.model.flow.dto.result.*;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;
import tech.wetech.flexmodel.domain.model.flow.exception.ReentrantException;
import tech.wetech.flexmodel.domain.model.flow.exception.TurboException;
import tech.wetech.flexmodel.domain.model.flow.executor.FlowExecutor;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowInstanceMappingRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.NodeInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.service.FlowInstanceService;
import tech.wetech.flexmodel.domain.model.flow.service.InstanceDataService;
import tech.wetech.flexmodel.domain.model.flow.service.NodeInstanceService;
import tech.wetech.flexmodel.domain.model.flow.shared.common.*;
import tech.wetech.flexmodel.domain.model.flow.shared.util.FlowModelUtil;
import tech.wetech.flexmodel.domain.model.flow.shared.util.InstanceDataUtil;
import tech.wetech.flexmodel.domain.model.flow.validator.ParamValidator;
import tech.wetech.flexmodel.shared.utils.CollectionUtils;
import tech.wetech.flexmodel.shared.utils.JsonUtils;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.util.*;

@Singleton
public class RuntimeProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeProcessor.class);

  @Inject
  FlowDeploymentRepository flowDeploymentRepository;

  @Inject
  FlowInstanceRepository processInstanceRepository;

  @Inject
  NodeInstanceRepository nodeInstanceRepository;

  @Inject
  FlowInstanceMappingRepository flowInstanceMappingRepository;

  @Inject
  FlowExecutor flowExecutor;

  @Inject
  FlowInstanceService flowInstanceService;

  @Inject
  InstanceDataService instanceDataService;

  @Inject
  NodeInstanceService nodeInstanceService;

  ////////////////////////////////////////startProcess////////////////////////////////////////

  public StartProcessResult startProcess(StartProcessParam startProcessParam) {
    RuntimeContext runtimeContext = null;
    try {
      ParamValidator.validate(startProcessParam);
      FlowInfo flowInfo = getFlowInfo(startProcessParam);
      runtimeContext = buildStartProcessContext(flowInfo, startProcessParam.getVariables(), startProcessParam.getRuntimeContext());
      flowExecutor.execute(runtimeContext);
      return buildStartProcessResult(runtimeContext);
    } catch (TurboException e) {
      if (!ErrorEnum.isSuccess(e.getErrNo())) {
        LOGGER.warn("startProcess ProcessException.||startProcessParam={}||runtimeContext={}, ",
          startProcessParam, runtimeContext, e);
      }
      return buildStartProcessResult(runtimeContext, e);
    }
  }

  private FlowInfo getFlowInfo(StartProcessParam startProcessParam) throws ProcessException {
    if (StringUtils.isNotBlank(startProcessParam.getFlowDeployId())) {
      return getFlowInfoByFlowDeployId(startProcessParam.getFlowDeployId());
    } else {
      return getFlowInfoByFlowModuleId(startProcessParam.getFlowModuleId());
    }
  }

  private RuntimeContext buildStartProcessContext(FlowInfo flowInfo, Map<String, Object> variables, RuntimeContext parentRuntimeContext) {
    return buildRuntimeContext(flowInfo, variables, parentRuntimeContext);
  }

  private StartProcessResult buildStartProcessResult(RuntimeContext runtimeContext) {
    StartProcessResult startProcessResult = JsonUtils.getInstance().convertValue(runtimeContext, StartProcessResult.class);
    return (StartProcessResult) fillRuntimeResult(startProcessResult, runtimeContext);
  }

  private StartProcessResult buildStartProcessResult(RuntimeContext runtimeContext, TurboException e) {
    StartProcessResult startProcessResult = JsonUtils.getInstance().convertValue(runtimeContext, StartProcessResult.class);
    return (StartProcessResult) fillRuntimeResult(startProcessResult, runtimeContext, e);
  }

  ////////////////////////////////////////commit////////////////////////////////////////

  public CommitTaskResult commit(CommitTaskParam commitTaskParam) {
    RuntimeContext runtimeContext = null;
    try {
      ParamValidator.validate(commitTaskParam);
      FlowInstanceBO flowInstanceBO = getFlowInstanceBO(commitTaskParam.getFlowInstanceId());
      if (flowInstanceBO.getStatus() == FlowInstanceStatus.TERMINATED) {
        LOGGER.warn("commit failed: flowInstance has been completed.||commitTaskParam={}", commitTaskParam);
        throw new ProcessException(ErrorEnum.COMMIT_REJECTRD);
      }
      if (flowInstanceBO.getStatus() == FlowInstanceStatus.COMPLETED) {
        LOGGER.warn("commit: reentrant process.||commitTaskParam={}", commitTaskParam);
        throw new ReentrantException(ErrorEnum.REENTRANT_WARNING);
      }
      String flowDeployId = flowInstanceBO.getFlowDeployId();
      FlowInfo flowInfo = getFlowInfoByFlowDeployId(flowDeployId);
      runtimeContext = buildCommitContext(commitTaskParam, flowInfo, flowInstanceBO.getStatus());
      flowExecutor.commit(runtimeContext);
      return buildCommitTaskResult(runtimeContext);
    } catch (TurboException e) {
      if (!ErrorEnum.isSuccess(e.getErrNo())) {
        LOGGER.warn("commit ProcessException.||commitTaskParam={}||runtimeContext={}, ", commitTaskParam, runtimeContext, e);
      }
      return buildCommitTaskResult(runtimeContext, e);
    }
  }

  private RuntimeContext buildCommitContext(CommitTaskParam commitTaskParam, FlowInfo flowInfo, int flowInstanceStatus) {
    RuntimeContext runtimeContext = buildRuntimeContext(flowInfo, commitTaskParam.getVariables(), commitTaskParam.getRuntimeContext());
    runtimeContext.setFlowInstanceId(commitTaskParam.getFlowInstanceId());
    runtimeContext.setFlowInstanceStatus(flowInstanceStatus);
    RuntimeContext parentRuntimeContext = runtimeContext.getParentRuntimeContext();
    String realNodeInstanceId = null;
    if (parentRuntimeContext == null) {
      Stack<String> nodeInstanceId2RootStack = flowInstanceService.getNodeInstanceIdStack(commitTaskParam.getFlowInstanceId(), commitTaskParam.getTaskInstanceId());
      runtimeContext.setSuspendNodeInstanceStack(nodeInstanceId2RootStack);
      realNodeInstanceId = nodeInstanceId2RootStack.isEmpty() ? commitTaskParam.getTaskInstanceId() : nodeInstanceId2RootStack.pop();
    } else {
      runtimeContext.setSuspendNodeInstanceStack(parentRuntimeContext.getSuspendNodeInstanceStack());
      realNodeInstanceId = commitTaskParam.getTaskInstanceId();
    }
    NodeInstanceBO suspendNodeInstance = new NodeInstanceBO();
    suspendNodeInstance.setNodeInstanceId(realNodeInstanceId);
    runtimeContext.setSuspendNodeInstance(suspendNodeInstance);
    runtimeContext.setCallActivityFlowModuleId(commitTaskParam.getCallActivityFlowModuleId());
    runtimeContext.setExtendProperties(commitTaskParam.getExtendProperties());
    return runtimeContext;
  }

  private CommitTaskResult buildCommitTaskResult(RuntimeContext runtimeContext) {
    CommitTaskResult commitTaskResult = runtimeContext == null ? new CommitTaskResult()
      : JsonUtils.getInstance().convertValue(runtimeContext, CommitTaskResult.class);
    return (CommitTaskResult) fillRuntimeResult(commitTaskResult, runtimeContext);
  }

  private CommitTaskResult buildCommitTaskResult(RuntimeContext runtimeContext, TurboException e) {
    CommitTaskResult commitTaskResult = runtimeContext == null ? new CommitTaskResult()
      : JsonUtils.getInstance().convertValue(runtimeContext, CommitTaskResult.class);
    return (CommitTaskResult) fillRuntimeResult(commitTaskResult, runtimeContext, e);
  }

  ////////////////////////////////////////rollback////////////////////////////////////////

  public RollbackTaskResult rollback(RollbackTaskParam rollbackTaskParam) {
    RuntimeContext runtimeContext = null;
    try {
      ParamValidator.validate(rollbackTaskParam);
      FlowInstanceBO flowInstanceBO = getFlowInstanceBO(rollbackTaskParam.getFlowInstanceId());
      if ((flowInstanceBO.getStatus() != FlowInstanceStatus.RUNNING) && (flowInstanceBO.getStatus() != FlowInstanceStatus.END)) {
        LOGGER.warn("rollback failed: invalid status to rollback.||rollbackTaskParam={}||status={}",
          rollbackTaskParam, flowInstanceBO.getStatus());
        throw new ProcessException(ErrorEnum.ROLLBACK_REJECTRD);
      }
      String flowDeployId = flowInstanceBO.getFlowDeployId();
      FlowInfo flowInfo = getFlowInfoByFlowDeployId(flowDeployId);
      runtimeContext = buildRollbackContext(rollbackTaskParam, flowInfo, flowInstanceBO.getStatus());
      flowExecutor.rollback(runtimeContext);
      return buildRollbackTaskResult(runtimeContext);
    } catch (TurboException e) {
      if (!ErrorEnum.isSuccess(e.getErrNo())) {
        LOGGER.warn("rollback ProcessException.||rollbackTaskParam={}||runtimeContext={}, ", rollbackTaskParam, runtimeContext, e);
      }
      return buildRollbackTaskResult(runtimeContext, e);
    }
  }

  private RuntimeContext buildRollbackContext(RollbackTaskParam rollbackTaskParam, FlowInfo flowInfo, int flowInstanceStatus) {
    RuntimeContext runtimeContext = buildRuntimeContext(flowInfo);
    runtimeContext.setFlowInstanceId(rollbackTaskParam.getFlowInstanceId());
    runtimeContext.setFlowInstanceStatus(flowInstanceStatus);
    RuntimeContext parentRuntimeContext = rollbackTaskParam.getRuntimeContext();
    String realNodeInstanceId = null;
    if (parentRuntimeContext == null) {
      Stack<String> nodeInstanceId2RootStack = flowInstanceService.getNodeInstanceIdStack(rollbackTaskParam.getFlowInstanceId(), rollbackTaskParam.getTaskInstanceId());
      runtimeContext.setSuspendNodeInstanceStack(nodeInstanceId2RootStack);
      realNodeInstanceId = nodeInstanceId2RootStack.isEmpty() ? rollbackTaskParam.getTaskInstanceId() : nodeInstanceId2RootStack.pop();
    } else {
      runtimeContext.setParentRuntimeContext(rollbackTaskParam.getRuntimeContext());
      runtimeContext.setSuspendNodeInstanceStack(rollbackTaskParam.getRuntimeContext().getSuspendNodeInstanceStack());
      realNodeInstanceId = rollbackTaskParam.getTaskInstanceId();
    }
    NodeInstanceBO suspendNodeInstance = new NodeInstanceBO();
    suspendNodeInstance.setNodeInstanceId(realNodeInstanceId);
    runtimeContext.setSuspendNodeInstance(suspendNodeInstance);
    runtimeContext.setExtendProperties(rollbackTaskParam.getExtendProperties());
    return runtimeContext;
  }

  private RollbackTaskResult buildRollbackTaskResult(RuntimeContext runtimeContext) {
    RollbackTaskResult rollbackTaskResult = runtimeContext == null ? new RollbackTaskResult()
      : JsonUtils.getInstance().convertValue(runtimeContext, RollbackTaskResult.class);
    return (RollbackTaskResult) fillRuntimeResult(rollbackTaskResult, runtimeContext);
  }

  private RollbackTaskResult buildRollbackTaskResult(RuntimeContext runtimeContext, TurboException e) {
    RollbackTaskResult rollbackTaskResult = runtimeContext == null ? new RollbackTaskResult()
      : JsonUtils.getInstance().convertValue(runtimeContext, RollbackTaskResult.class);
    return (RollbackTaskResult) fillRuntimeResult(rollbackTaskResult, runtimeContext, e);
  }

  ////////////////////////////////////////terminate////////////////////////////////////////

  public TerminateResult terminateProcess(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    TerminateResult terminateResult;
    try {
      int flowInstanceStatus;
      FlowInstance flowInstancePO = processInstanceRepository.selectByFlowInstanceId(flowInstanceId);
      if (flowInstancePO == null) {
        LOGGER.warn("terminateProcess failed: cannot find flowInstancePO from db.||flowInstanceId={}", flowInstanceId);
        throw new ProcessException(ErrorEnum.GET_FLOW_INSTANCE_FAILED);
      }
      if (flowInstancePO.getStatus() == FlowInstanceStatus.COMPLETED) {
        LOGGER.warn("terminateProcess: flowInstance is completed.||flowInstanceId={}", flowInstanceId);
        flowInstanceStatus = FlowInstanceStatus.COMPLETED;
      } else {
        processInstanceRepository.updateStatus(flowInstancePO, FlowInstanceStatus.TERMINATED);
        flowInstanceStatus = FlowInstanceStatus.TERMINATED;
      }
      if (effectiveForSubFlowInstance) {
        terminateSubFlowInstance(flowInstanceId);
      }
      terminateResult = new TerminateResult(ErrorEnum.SUCCESS);
      terminateResult.setFlowInstanceId(flowInstanceId);
      terminateResult.setStatus(flowInstanceStatus);
    } catch (Exception e) {
      LOGGER.error("terminateProcess exception.||flowInstanceId={}, ", flowInstanceId, e);
      terminateResult = new TerminateResult(ErrorEnum.SYSTEM_ERROR);
      terminateResult.setFlowInstanceId(flowInstanceId);
    }
    return terminateResult;
  }

  public void terminateSubFlowInstance(String flowInstanceId) {
    Set<String> allSubFlowInstanceIds = flowInstanceService.getAllSubFlowInstanceIds(flowInstanceId);
    for (String subFlowInstanceId : allSubFlowInstanceIds) {
      terminateProcess(subFlowInstanceId, false);
    }
  }

  ////////////////////////////////////////getHistoryUserTaskList////////////////////////////////////////

  public NodeInstanceListResult getHistoryUserTaskList(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    List<NodeInstance> historyNodeInstanceList = getDescHistoryNodeInstanceList(flowInstanceId);
    NodeInstanceListResult historyListResult = new NodeInstanceListResult(ErrorEnum.SUCCESS);
    historyListResult.setNodeInstanceList(new ArrayList<>());
    try {
      if (CollectionUtils.isEmpty(historyNodeInstanceList)) {
        LOGGER.warn("getHistoryUserTaskList: historyNodeInstanceList is empty.||flowInstanceId={}", flowInstanceId);
        return historyListResult;
      }
      String flowDeployId = historyNodeInstanceList.get(0).getFlowDeployId();
      Map<String, FlowElement> flowElementMap = getFlowElementMap(flowDeployId);
      List<tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance> userTaskList = historyListResult.getNodeInstanceList();
      for (NodeInstance nodeInstancePO : historyNodeInstanceList) {
        if (!isEffectiveNodeInstance(nodeInstancePO.getStatus())) {
          continue;
        }
        if (effectiveForSubFlowInstance && isCallActivity(nodeInstancePO.getNodeKey(), flowElementMap)) {
          //handle subFlowInstance
          String subFlowInstanceId = getExecuteSubFlowInstanceId(flowInstanceId, nodeInstancePO.getNodeInstanceId());
          if (StringUtils.isNotBlank(subFlowInstanceId)) {
            NodeInstanceListResult historyUserTaskList = getHistoryUserTaskList(subFlowInstanceId, true);
            userTaskList.addAll(historyUserTaskList.getNodeInstanceList());
          }
          continue;
        }

        //ignore un-userTask instance
        if (!isUserTask(nodeInstancePO.getNodeKey(), flowElementMap)) {
          continue;
        }

        //build effective userTask instance
        tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance nodeInstance = JsonUtils.getInstance().convertValue(nodeInstancePO, tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance.class);
        FlowElement flowElement = FlowModelUtil.getFlowElement(flowElementMap, nodeInstancePO.getNodeKey());
        nodeInstance.setModelKey(flowElement.getKey());
        nodeInstance.setModelName(FlowModelUtil.getElementName(flowElement));
        Map<String, Object> props = flowElement.getProperties();
        if (props != null && !props.isEmpty()) {
          nodeInstance.setProperties(props);
        } else {
          nodeInstance.setProperties(new HashMap<>());
        }
        userTaskList.add(nodeInstance);
      }
    } catch (ProcessException e) {
      historyListResult.setErrCode(e.getErrNo());
      historyListResult.setErrMsg(e.getErrMsg());
    }
    return historyListResult;
  }

  private Map<String, FlowElement> getFlowElementMap(String flowDeployId) throws ProcessException {
    FlowInfo flowInfo = getFlowInfoByFlowDeployId(flowDeployId);
    String flowModel = flowInfo.getFlowModel();
    return FlowModelUtil.getFlowElementMap(flowModel);
  }

  private boolean isEffectiveNodeInstance(int status) {
    return status == NodeInstanceStatus.COMPLETED || status == NodeInstanceStatus.ACTIVE;
  }

  private boolean isUserTask(String nodeKey, Map<String, FlowElement> flowElementMap) throws ProcessException {
    int type = getNodeType(nodeKey, flowElementMap);
    return type == FlowElementType.USER_TASK;
  }

  private int getNodeType(String nodeKey, Map<String, FlowElement> flowElementMap) throws ProcessException {
    if (!flowElementMap.containsKey(nodeKey)) {
      LOGGER.warn("isUserTask: invalid nodeKey which is not in flowElementMap.||nodeKey={}||flowElementMap={}",
        nodeKey, flowElementMap);
      throw new ProcessException(ErrorEnum.GET_NODE_FAILED);
    }
    FlowElement flowElement = flowElementMap.get(nodeKey);
    return flowElement.getType();
  }

  private boolean isCallActivity(String nodeKey, Map<String, FlowElement> flowElementMap) throws ProcessException {
    int type = getNodeType(nodeKey, flowElementMap);
    return type == FlowElementType.CALL_ACTIVITY;
  }

  ////////////////////////////////////////getHistoryElementList////////////////////////////////////////

  public ElementInstanceListResult getHistoryElementList(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    List<NodeInstance> historyNodeInstanceList = getHistoryNodeInstanceList(flowInstanceId);
    ElementInstanceListResult elementInstanceListResult = new ElementInstanceListResult(ErrorEnum.SUCCESS);
    elementInstanceListResult.setElementInstanceList(new ArrayList<>());
    try {
      if (CollectionUtils.isEmpty(historyNodeInstanceList)) {
        LOGGER.warn("getHistoryElementList: historyNodeInstanceList is empty.||flowInstanceId={}", flowInstanceId);
        return elementInstanceListResult;
      }
      String flowDeployId = historyNodeInstanceList.get(0).getFlowDeployId();
      Map<String, FlowElement> flowElementMap = getFlowElementMap(flowDeployId);
      List<ElementInstance> elementInstanceList = elementInstanceListResult.getElementInstanceList();
      for (NodeInstance nodeInstancePO : historyNodeInstanceList) {
        String nodeKey = nodeInstancePO.getNodeKey();
        String sourceNodeKey = nodeInstancePO.getSourceNodeKey();
        int nodeStatus = nodeInstancePO.getStatus();
        String nodeInstanceId = nodeInstancePO.getNodeInstanceId();
        String instanceDataId = nodeInstancePO.getInstanceDataId();
        if (StringUtils.isNotBlank(sourceNodeKey)) {
          FlowElement sourceFlowElement = FlowModelUtil.getSequenceFlow(flowElementMap, sourceNodeKey, nodeKey);
          if (sourceFlowElement == null) {
            LOGGER.error("getHistoryElementList failed: sourceFlowElement is null.||nodeKey={}||sourceNodeKey={}||flowElementMap={}", nodeKey, sourceNodeKey, flowElementMap);
            throw new ProcessException(ErrorEnum.MODEL_UNKNOWN_ELEMENT_KEY);
          }
          int sourceSequenceFlowStatus = nodeStatus;
          if (nodeStatus == NodeInstanceStatus.ACTIVE) {
            sourceSequenceFlowStatus = NodeInstanceStatus.COMPLETED;
          }
          ElementInstance sequenceFlowInstance = new ElementInstance(sourceFlowElement.getKey(), sourceSequenceFlowStatus, null, null);
          elementInstanceList.add(sequenceFlowInstance);
        }
        ElementInstance nodeInstance = new ElementInstance(nodeKey, nodeStatus, nodeInstanceId, instanceDataId);
        elementInstanceList.add(nodeInstance);
        if (!FlowModelUtil.isElementType(nodeKey, flowElementMap, FlowElementType.CALL_ACTIVITY)) {
          continue;
        }
        if (!effectiveForSubFlowInstance) {
          continue;
        }
        List<FlowInstanceMapping> flowInstanceMappingPOS = flowInstanceMappingRepository.selectFlowInstanceMappingList(flowInstanceId, nodeInstanceId);
        List<ElementInstance> subElementInstanceList = new ArrayList<>();
        nodeInstance.setSubElementInstanceList(subElementInstanceList);
        for (FlowInstanceMapping flowInstanceMappingPO : flowInstanceMappingPOS) {
          ElementInstanceListResult subElementInstanceListResult = getHistoryElementList(flowInstanceMappingPO.getSubFlowInstanceId(), effectiveForSubFlowInstance);
          subElementInstanceList.addAll(subElementInstanceListResult.getElementInstanceList());
        }
      }
    } catch (ProcessException e) {
      elementInstanceListResult.setErrCode(e.getErrNo());
      elementInstanceListResult.setErrMsg(e.getErrMsg());
    }
    return elementInstanceListResult;
  }

  private String getExecuteSubFlowInstanceId(String flowInstanceId, String nodeInstanceId) {
    List<FlowInstanceMapping> flowInstanceMappingPOList = flowInstanceMappingRepository.selectFlowInstanceMappingList(flowInstanceId, nodeInstanceId);
    if (CollectionUtils.isEmpty(flowInstanceMappingPOList)) {
      return null;
    }
    for (FlowInstanceMapping flowInstanceMappingPO : flowInstanceMappingPOList) {
      if (FlowInstanceMappingType.EXECUTE == flowInstanceMappingPO.getType()) {
        return flowInstanceMappingPO.getSubFlowInstanceId();
      }
    }
    return flowInstanceMappingPOList.get(0).getSubFlowInstanceId();
  }

  private List<NodeInstance> getHistoryNodeInstanceList(String flowInstanceId) {
    return nodeInstanceRepository.selectByFlowInstanceId(flowInstanceId);
  }

  private List<NodeInstance> getDescHistoryNodeInstanceList(String flowInstanceId) {
    return nodeInstanceRepository.selectDescByFlowInstanceId(flowInstanceId);
  }

  public NodeInstanceResult getNodeInstance(String flowInstanceId, String nodeInstanceId, boolean effectiveForSubFlowInstance) {
    NodeInstanceResult nodeInstanceResult = new NodeInstanceResult();
    try {
      NodeInstance nodeInstancePO = nodeInstanceService.selectByNodeInstanceId(flowInstanceId, nodeInstanceId, effectiveForSubFlowInstance);
      String flowDeployId = nodeInstancePO.getFlowDeployId();
      Map<String, FlowElement> flowElementMap = getFlowElementMap(flowDeployId);
      tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance nodeInstance = JsonUtils.getInstance().convertValue(nodeInstancePO, tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance.class);
      FlowElement flowElement = FlowModelUtil.getFlowElement(flowElementMap, nodeInstancePO.getNodeKey());
      nodeInstance.setModelKey(flowElement.getKey());
      nodeInstance.setModelName(FlowModelUtil.getElementName(flowElement));
      Map<String, Object> props = flowElement.getProperties();
      if (props != null && !props.isEmpty()) {
        nodeInstance.setProperties(props);
      } else {
        nodeInstance.setProperties(new HashMap<>());
      }
      nodeInstanceResult.setNodeInstance(nodeInstance);
      nodeInstanceResult.setErrCode(ErrorEnum.SUCCESS.getErrNo());
      nodeInstanceResult.setErrMsg(ErrorEnum.SUCCESS.getErrMsg());
    } catch (ProcessException e) {
      nodeInstanceResult.setErrCode(e.getErrNo());
      nodeInstanceResult.setErrMsg(e.getErrMsg());
    }
    return nodeInstanceResult;
  }

  ////////////////////////////////////////getInstanceData////////////////////////////////////////
  public InstanceDataListResult getInstanceData(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    tech.wetech.flexmodel.codegen.entity.InstanceData instanceDataPO = instanceDataService.select(flowInstanceId, effectiveForSubFlowInstance);
    return packageInstanceDataResult(instanceDataPO);
  }

  public InstanceDataListResult getInstanceData(String flowInstanceId, String instanceDataId, boolean effectiveForSubFlowInstance) {
    tech.wetech.flexmodel.codegen.entity.InstanceData instanceDataPO = instanceDataService.select(flowInstanceId, instanceDataId, effectiveForSubFlowInstance);
    return packageInstanceDataResult(instanceDataPO);
  }

  public InstanceDataListResult packageInstanceDataResult(tech.wetech.flexmodel.codegen.entity.InstanceData instanceDataPO) {
    Map<String, Object> instanceDataMap = InstanceDataUtil.getInstanceDataMap(instanceDataPO.getInstanceData());
    InstanceDataListResult instanceDataListResult = new InstanceDataListResult(ErrorEnum.SUCCESS);
    instanceDataListResult.setVariables(instanceDataMap);
    return instanceDataListResult;
  }

  public FlowInstanceResult getFlowInstance(String flowInstanceId) {
    FlowInstanceResult flowInstanceResult = new FlowInstanceResult();
    try {
      FlowInstanceBO flowInstanceBO = getFlowInstanceBO(flowInstanceId);
      flowInstanceResult.setFlowInstance(flowInstanceBO);
    } catch (ProcessException e) {
      flowInstanceResult.setErrCode(e.getErrNo());
      flowInstanceResult.setErrMsg(e.getErrMsg());
    }
    return flowInstanceResult;
  }

  ////////////////////////////////////////common////////////////////////////////////////

  private FlowInfo getFlowInfoByFlowDeployId(String flowDeployId) throws ProcessException {
    FlowDeployment flowDeploymentPO = flowDeploymentRepository.findByDeployId(flowDeployId);
    if (flowDeploymentPO == null) {
      LOGGER.warn("getFlowInfoByFlowDeployId failed.||flowDeployId={}", flowDeployId);
      throw new ProcessException(ErrorEnum.GET_FLOW_DEPLOYMENT_FAILED);
    }
    FlowInfo flowInfo = JsonUtils.getInstance().convertValue(flowDeploymentPO, FlowInfo.class);
    return flowInfo;
  }

  private FlowInfo getFlowInfoByFlowModuleId(String flowModuleId) throws ProcessException {
    FlowDeployment flowDeploymentPO = flowDeploymentRepository.findRecentByFlowModuleId(flowModuleId);
    if (flowDeploymentPO == null) {
      LOGGER.warn("getFlowInfoByFlowModuleId failed.||flowModuleId={}", flowModuleId);
      throw new ProcessException(ErrorEnum.GET_FLOW_DEPLOYMENT_FAILED);
    }
    FlowInfo flowInfo = JsonUtils.getInstance().convertValue(flowDeploymentPO, FlowInfo.class);
    return flowInfo;
  }

  private FlowInstanceBO getFlowInstanceBO(String flowInstanceId) throws ProcessException {
    FlowInstance flowInstancePO = processInstanceRepository.selectByFlowInstanceId(flowInstanceId);
    if (flowInstancePO == null) {
      LOGGER.warn("getFlowInstancePO failed: cannot find flowInstancePO from db.||flowInstanceId={}", flowInstanceId);
      throw new ProcessException(ErrorEnum.GET_FLOW_INSTANCE_FAILED);
    }
    return JsonUtils.getInstance().convertValue(flowInstancePO, FlowInstanceBO.class);
  }

  private RuntimeContext buildRuntimeContext(FlowInfo flowInfo) {
    RuntimeContext runtimeContext = JsonUtils.getInstance().convertValue(flowInfo, RuntimeContext.class);
    runtimeContext.setFlowElementMap(FlowModelUtil.getFlowElementMap(flowInfo.getFlowModel()));
    return runtimeContext;
  }

  private RuntimeContext buildRuntimeContext(FlowInfo flowInfo, Map<String, Object> variables, RuntimeContext parentRuntimeContext) {
    RuntimeContext runtimeContext = buildRuntimeContext(flowInfo);
    runtimeContext.setInstanceDataMap(variables);
    runtimeContext.setParentRuntimeContext(parentRuntimeContext);
    return runtimeContext;
  }

  private RuntimeResult fillRuntimeResult(RuntimeResult runtimeResult, RuntimeContext runtimeContext) {
    if (runtimeContext.getProcessStatus() == ProcessStatus.SUCCESS) {
      return fillRuntimeResult(runtimeResult, runtimeContext, ErrorEnum.SUCCESS);
    }
    return fillRuntimeResult(runtimeResult, runtimeContext, ErrorEnum.FAILED);
  }

  private RuntimeResult fillRuntimeResult(RuntimeResult runtimeResult, RuntimeContext runtimeContext, ErrorEnum errorEnum) {
    return fillRuntimeResult(runtimeResult, runtimeContext, errorEnum.getErrNo(), errorEnum.getErrMsg());
  }

  private RuntimeResult fillRuntimeResult(RuntimeResult runtimeResult, RuntimeContext runtimeContext, TurboException e) {
    return fillRuntimeResult(runtimeResult, runtimeContext, e.getErrNo(), e.getErrMsg());
  }

  private RuntimeResult fillRuntimeResult(RuntimeResult runtimeResult, RuntimeContext runtimeContext, int errNo, String errMsg) {
    runtimeResult.setErrCode(errNo);
    runtimeResult.setErrMsg(errMsg);
    if (runtimeContext != null) {
      runtimeResult.setFlowInstanceId(runtimeContext.getFlowInstanceId());
      runtimeResult.setStatus(runtimeContext.getFlowInstanceStatus());
      List<RuntimeResult.NodeExecuteResult> nodeExecuteResults = new ArrayList<>();
      if (null != runtimeContext.getExtendRuntimeContextList() && !runtimeContext.getExtendRuntimeContextList().isEmpty()) {
        for (ExtendRuntimeContext extendRuntimeContext : runtimeContext.getExtendRuntimeContextList()) {
          RuntimeResult.NodeExecuteResult result = new RuntimeResult.NodeExecuteResult();
          result.setActiveTaskInstance(buildActiveTaskInstance(extendRuntimeContext.getBranchSuspendNodeInstance(), runtimeContext));
          result.setVariables(extendRuntimeContext.getBranchExecuteDataMap());
          result.setErrCode(extendRuntimeContext.getException().getErrNo());
          result.setErrMsg(extendRuntimeContext.getException().getErrMsg());
          nodeExecuteResults.add(result);
        }
      } else {
        RuntimeResult.NodeExecuteResult result = new RuntimeResult.NodeExecuteResult();
        result.setActiveTaskInstance(buildActiveTaskInstance(runtimeContext.getSuspendNodeInstance(), runtimeContext));
        result.setVariables(runtimeContext.getInstanceDataMap());
        nodeExecuteResults.add(result);
      }
      runtimeResult.setNodeExecuteResults(nodeExecuteResults);
    }
    return runtimeResult;
  }

  private tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance buildActiveTaskInstance(NodeInstanceBO nodeInstanceBO, RuntimeContext runtimeContext) {
    tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance activeNodeInstance = JsonUtils.getInstance().convertValue(nodeInstanceBO, tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance.class);
    activeNodeInstance.setModelKey(nodeInstanceBO.getNodeKey());
    FlowElement flowElement = runtimeContext.getFlowElementMap().get(nodeInstanceBO.getNodeKey());
    activeNodeInstance.setModelName(FlowModelUtil.getElementName(flowElement));
    activeNodeInstance.setProperties(flowElement.getProperties());
    activeNodeInstance.setFlowElementType(flowElement.getType());
    activeNodeInstance.setSubNodeResultList(runtimeContext.getCallActivityRuntimeResultList());
    return activeNodeInstance;
  }

  public void checkIsSubFlowInstance(String flowInstanceId) {
    FlowInstance flowInstancePO = processInstanceRepository.selectByFlowInstanceId(flowInstanceId);
    if (flowInstancePO == null) {
      LOGGER.warn("checkIsSubFlowInstance failed: cannot find flowInstancePO from db.||flowInstanceId={}", flowInstanceId);
      throw new RuntimeException(ErrorEnum.GET_FLOW_INSTANCE_FAILED.getErrMsg());
    }
    if (StringUtils.isNotBlank(flowInstancePO.getParentFlowInstanceId())) {
      LOGGER.error("checkIsSubFlowInstance failed: don't receive sub-processes.||flowInstanceId={}", flowInstanceId);
      throw new RuntimeException(ErrorEnum.NO_RECEIVE_SUB_FLOW_INSTANCE.getErrMsg());
    }
  }
}
