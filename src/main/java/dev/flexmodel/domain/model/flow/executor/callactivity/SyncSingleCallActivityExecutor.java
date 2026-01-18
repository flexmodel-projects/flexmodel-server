package dev.flexmodel.domain.model.flow.executor.callactivity;

import dev.flexmodel.domain.model.flow.shared.common.*;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.codegen.entity.FlowDeployment;
import dev.flexmodel.codegen.entity.FlowInstance;
import dev.flexmodel.codegen.entity.FlowInstanceMapping;
import dev.flexmodel.codegen.entity.NodeInstance;
import dev.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.dto.param.CommitTaskParam;
import dev.flexmodel.domain.model.flow.dto.param.RollbackTaskParam;
import dev.flexmodel.domain.model.flow.dto.param.StartProcessParam;
import dev.flexmodel.domain.model.flow.dto.result.CommitTaskResult;
import dev.flexmodel.domain.model.flow.dto.result.RollbackTaskResult;
import dev.flexmodel.domain.model.flow.dto.result.RuntimeResult;
import dev.flexmodel.domain.model.flow.dto.result.StartProcessResult;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.exception.SuspendException;
import dev.flexmodel.domain.model.flow.shared.common.*;
import dev.flexmodel.domain.model.flow.shared.util.FlowModelUtil;
import dev.flexmodel.domain.model.flow.shared.util.InstanceDataUtil;
import dev.flexmodel.shared.utils.CollectionUtils;
import dev.flexmodel.shared.utils.JsonUtils;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * CallActivityExecutor base on sync and single instance mode,
 * support the dynamic assignment of subFlowModule on the execution side
 * <p>
 * feature e.g.
 * 1.Automatically suspend when executing to CallActivity node
 * 2.External systems can attach unique attributes on CallActivity node
 * 3.When External systems compute subFlowModuleId success, need continue to submit downward
 * 4.CallActivity node support repeated submission
 */
@Singleton
public class SyncSingleCallActivityExecutor extends AbstractCallActivityExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncSingleCallActivityExecutor.class);

  @Override
  protected void doExecute(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    if (currentNodeInstance.getStatus() == NodeInstanceStatus.COMPLETED) {
      LOGGER.warn("doExecute reentrant: currentNodeInstance is completed.||runtimeContext={}", runtimeContext);
      return;
    }

    if (currentNodeInstance.getStatus() != NodeInstanceStatus.ACTIVE) {
      currentNodeInstance.setStatus(NodeInstanceStatus.ACTIVE);
    }
    runtimeContext.getNodeInstanceList().add(currentNodeInstance);

    FlowElement flowElement = runtimeContext.getCurrentNodeModel();
    String nodeName = FlowModelUtil.getElementName(flowElement);
    LOGGER.info("doExecute: syncSingleCallActivity to commit.||flowInstanceId={}||nodeInstanceId={}||nodeKey={}||nodeName={}",
      runtimeContext.getFlowInstanceId(), currentNodeInstance.getNodeInstanceId(), flowElement.getKey(), nodeName);
    throw new SuspendException(ErrorEnum.COMMIT_SUSPEND, MessageFormat.format(Constants.NODE_INSTANCE_FORMAT,
      flowElement.getKey(), nodeName, currentNodeInstance.getNodeInstanceId()));
  }

  @Override
  protected void preCommit(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO suspendNodeInstance = runtimeContext.getSuspendNodeInstance();
    NodeInstanceBO currentNodeInstance = JsonUtils.getInstance().convertValue(suspendNodeInstance, NodeInstanceBO.class);
    runtimeContext.setCurrentNodeInstance(currentNodeInstance);
  }

  @Override
  protected void doCommit(RuntimeContext runtimeContext) throws ProcessException {
    boolean commitCallActivityNode = CollectionUtils.isEmpty(runtimeContext.getSuspendNodeInstanceStack());
    if (commitCallActivityNode) {
      startProcessCallActivity(runtimeContext);
    } else {
      commitCallActivity(runtimeContext);
    }
  }

  @Override
  protected void postCommit(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    runtimeContext.getNodeInstanceList().add(currentNodeInstance);
  }

  @Override
  protected void doRollback(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    FlowInstanceMapping flowInstanceMappingPO = flowInstanceMappingRepository.selectFlowInstanceMapping(runtimeContext.getProjectId(), runtimeContext.getFlowInstanceId(), currentNodeInstance.getNodeInstanceId());
    String subFlowInstanceId = flowInstanceMappingPO.getSubFlowInstanceId();

    String taskInstanceId = null;
    if (CollectionUtils.isEmpty(runtimeContext.getSuspendNodeInstanceStack())) {
      NodeInstance nodeInstancePO = nodeInstanceService.selectRecentEndNode(runtimeContext.getProjectId(), subFlowInstanceId);
      taskInstanceId = nodeInstancePO.getNodeInstanceId();
    } else {
      taskInstanceId = runtimeContext.getSuspendNodeInstanceStack().pop();
    }

    RollbackTaskParam rollbackTaskParam = new RollbackTaskParam();
    rollbackTaskParam.setRuntimeContext(runtimeContext);
    rollbackTaskParam.setFlowInstanceId(subFlowInstanceId);
    rollbackTaskParam.setTaskInstanceId(taskInstanceId);
    rollbackTaskParam.setProjectId(runtimeContext.getProjectId());
    RollbackTaskResult rollbackTaskResult = runtimeProcessorInstance.get().rollback(rollbackTaskParam);
    LOGGER.info("callActivity rollback.||rollbackTaskParam={}||rollbackTaskResult={}", rollbackTaskParam, rollbackTaskResult);
    // 4.update flowInstance mapping
    updateFlowInstanceMapping(runtimeContext);
    handleCallActivityResult(runtimeContext, rollbackTaskResult);
  }

  @Override
  protected void postRollback(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    runtimeContext.getNodeInstanceList().add(currentNodeInstance);
  }

  protected void startProcessCallActivity(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    // 1.check reentrant execute
    FlowInstanceMapping flowInstanceMappingPO = flowInstanceMappingRepository.selectFlowInstanceMapping(runtimeContext.getProjectId(), runtimeContext.getFlowInstanceId(), currentNodeInstance.getNodeInstanceId());
    if (flowInstanceMappingPO != null) {
      handleReentrantSubFlowInstance(runtimeContext, flowInstanceMappingPO);
      return;
    }
    // 2.check CallActivity nested level
    preCheckCallActivityNestedLevel(runtimeContext);

    // 3.get flowModuleId
    String callActivityFlowModuleId = runtimeContext.getCallActivityFlowModuleId();
    runtimeContext.setCallActivityFlowModuleId(null); // avoid misuse
    // 4.calculate variables
    Map<String, Object> callActivityVariables = getCallActivityVariables(runtimeContext);

    StartProcessParam startProcessParam = new StartProcessParam();
    startProcessParam.setRuntimeContext(runtimeContext);
    startProcessParam.setFlowModuleId(callActivityFlowModuleId);
    startProcessParam.setVariables(callActivityVariables);
    StartProcessResult startProcessResult = runtimeProcessorInstance.get().startProcess(startProcessParam);
    LOGGER.info("callActivity startProcess.||startProcessParam={}||startProcessResult={}", startProcessParam, startProcessResult);
    // 5.save flowInstance mapping
    saveFlowInstanceMapping(runtimeContext, startProcessResult.getFlowInstanceId());
    handleCallActivityResult(runtimeContext, startProcessResult);
  }

  private void preCheckCallActivityNestedLevel(RuntimeContext runtimeContext) throws ProcessException {
    int maxCallActivityNestedLevel = businessConfig.getCallActivityNestedLevel(runtimeContext.getCaller());
    int currentCallActivityNestedLevel = 0;
    RuntimeContext tmpRuntimeContext = runtimeContext;
    while (tmpRuntimeContext != null) {
      currentCallActivityNestedLevel++;
      tmpRuntimeContext = tmpRuntimeContext.getParentRuntimeContext();
    }
    if (maxCallActivityNestedLevel < currentCallActivityNestedLevel) {
      throw new ProcessException(ErrorEnum.FLOW_NESTED_LEVEL_EXCEEDED);
    }
  }

  private void saveFlowInstanceMapping(RuntimeContext runtimeContext, String subFlowInstanceId) {
    FlowInstanceMapping flowInstanceMapping = new FlowInstanceMapping();
    flowInstanceMapping.setProjectId(runtimeContext.getProjectId());
    flowInstanceMapping.setFlowInstanceId(runtimeContext.getFlowInstanceId());
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    flowInstanceMapping.setNodeKey(currentNodeInstance.getNodeKey());
    flowInstanceMapping.setNodeInstanceId(currentNodeInstance.getNodeInstanceId());
    flowInstanceMapping.setSubFlowInstanceId(subFlowInstanceId);
    flowInstanceMapping.setType(FlowInstanceMappingType.EXECUTE);
    flowInstanceMapping.setProjectId(runtimeContext.getProjectId());
    flowInstanceMapping.setCaller(runtimeContext.getCaller());
    flowInstanceMapping.setCreateTime(LocalDateTime.now());
    flowInstanceMapping.setModifyTime(LocalDateTime.now());
    flowInstanceMappingRepository.insert(flowInstanceMapping);
  }

  private void handleReentrantSubFlowInstance(RuntimeContext runtimeContext, FlowInstanceMapping flowInstanceMappingPO) throws ProcessException {
    String subFlowInstanceId = flowInstanceMappingPO.getSubFlowInstanceId();
    RuntimeResult subFlowInstanceFirstUserTask = getSubFlowInstanceFirstUserTask(runtimeContext.getProjectId(), subFlowInstanceId);
    if (subFlowInstanceFirstUserTask != null) {
      runtimeContext.setCallActivityRuntimeResultList(Arrays.asList(subFlowInstanceFirstUserTask));
      throw new SuspendException(ErrorEnum.COMMIT_SUSPEND);
    }
    LOGGER.info("callActivity did not find userTask.||subFlowInstanceId={}", subFlowInstanceId);
  }

  private RuntimeResult getSubFlowInstanceFirstUserTask(String projectId, String subFlowInstanceId) {
    FlowInstance subFlowInstance = processInstanceRepository.selectByFlowInstanceId(projectId, subFlowInstanceId);
    FlowDeployment subFlowDeploymentPO = flowDeploymentRepository.findByDeployId(projectId, subFlowInstance.getFlowDeployId());
    Map<String, FlowElement> subFlowElementMap = FlowModelUtil.getFlowElementMap(subFlowDeploymentPO.getFlowModel());

    List<dev.flexmodel.codegen.entity.NodeInstance> nodeInstancePOList = nodeInstanceRepository.selectByFlowInstanceId(projectId, subFlowInstanceId);
    for (dev.flexmodel.codegen.entity.NodeInstance nodeInstancePO : nodeInstancePOList) {
      int elementType = FlowModelUtil.getElementType(nodeInstancePO.getNodeKey(), subFlowElementMap);
      if (elementType == FlowElementType.USER_TASK) {
        return buildCallActivityFirstUserTaskRuntimeResult(projectId, subFlowInstance, subFlowElementMap, nodeInstancePO);
      } else if (elementType == FlowElementType.CALL_ACTIVITY) {
        FlowInstanceMapping flowInstanceMapping = flowInstanceMappingRepository.selectFlowInstanceMapping(projectId, subFlowInstanceId, nodeInstancePO.getNodeInstanceId());
        if (flowInstanceMapping == null) {
          LOGGER.warn("callActivity did not find instanceMapping.||subFlowInstanceId={}", subFlowInstanceId);
          break;
        }
        RuntimeResult runtimeResult = getSubFlowInstanceFirstUserTask(projectId, flowInstanceMapping.getSubFlowInstanceId());
        if (runtimeResult != null) {
          return runtimeResult;
        }
      }
    }
    return null;
  }

  private RuntimeResult buildCallActivityFirstUserTaskRuntimeResult(String projectId, FlowInstance subFlowInstance, Map<String, FlowElement> subFlowElementMap, dev.flexmodel.codegen.entity.NodeInstance nodeInstancePO) {
    RuntimeResult runtimeResult = new RuntimeResult();
    runtimeResult.setErrCode(ErrorEnum.COMMIT_SUSPEND.getErrNo());
    runtimeResult.setErrMsg(ErrorEnum.COMMIT_SUSPEND.getErrMsg());
    runtimeResult.setFlowInstanceId(subFlowInstance.getFlowInstanceId());
    runtimeResult.setStatus(subFlowInstance.getStatus());

    dev.flexmodel.domain.model.flow.dto.bo.NodeInstance nodeInstance = JsonUtils.getInstance().convertValue(nodeInstancePO, dev.flexmodel.domain.model.flow.dto.bo.NodeInstance.class);
    nodeInstance.setCreateTime(null);
    nodeInstance.setModifyTime(null);
    nodeInstance.setKey(nodeInstancePO.getNodeKey());
    FlowElement flowElement = subFlowElementMap.get(nodeInstancePO.getNodeKey());
    nodeInstance.setName(FlowModelUtil.getElementName(flowElement));
    nodeInstance.setProperties(flowElement.getProperties());

    runtimeResult.setActiveTaskInstance(nodeInstance);
    dev.flexmodel.codegen.entity.InstanceData instanceDataPO = instanceDataRepository.select(projectId, subFlowInstance.getFlowInstanceId(), nodeInstancePO.getInstanceDataId());
    Map<String, Object> instanceDataMap = InstanceDataUtil.getInstanceDataMap(instanceDataPO.getInstanceData());
    runtimeResult.setVariables(instanceDataMap);
    return runtimeResult;
  }

  protected void commitCallActivity(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO suspendNodeInstance = runtimeContext.getSuspendNodeInstance();
    FlowInstanceMapping flowInstanceMappingPO = flowInstanceMappingRepository.selectFlowInstanceMapping(runtimeContext.getProjectId(), runtimeContext.getFlowInstanceId(), suspendNodeInstance.getNodeInstanceId());
    String subFlowInstanceId = flowInstanceMappingPO.getSubFlowInstanceId();

    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setRuntimeContext(runtimeContext);
    commitTaskParam.setFlowInstanceId(subFlowInstanceId);
    commitTaskParam.setTaskInstanceId(runtimeContext.getSuspendNodeInstanceStack().pop());
    commitTaskParam.setVariables(runtimeContext.getInstanceDataMap());
    // transparent transmission callActivity param
    commitTaskParam.setCallActivityFlowModuleId(runtimeContext.getCallActivityFlowModuleId());
    commitTaskParam.setProjectId(runtimeContext.getProjectId());
    runtimeContext.setCallActivityFlowModuleId(null); // avoid misuse
    CommitTaskResult commitTaskResult = runtimeProcessorInstance.get().commit(commitTaskParam);
    LOGGER.info("callActivity commit.||commitTaskParam={}||commitTaskResult={}", commitTaskParam, commitTaskResult);
    handleCallActivityResult(runtimeContext, commitTaskResult);
  }

  private void updateFlowInstanceMapping(RuntimeContext runtimeContext) {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    if (currentNodeInstance.getStatus() != NodeInstanceStatus.COMPLETED) {
      return;
    }
    currentNodeInstance.setStatus(NodeInstanceStatus.DISABLED);
    runtimeContext.getNodeInstanceList().add(currentNodeInstance);

    NodeInstanceBO newNodeInstanceBO = JsonUtils.getInstance().convertValue(currentNodeInstance, NodeInstanceBO.class);
    newNodeInstanceBO.setId(null);
    String newNodeInstanceId = genId();
    newNodeInstanceBO.setNodeInstanceId(newNodeInstanceId);
    newNodeInstanceBO.setStatus(NodeInstanceStatus.ACTIVE);
    runtimeContext.setCurrentNodeInstance(newNodeInstanceBO);

    FlowInstanceMapping oldFlowInstanceMappingPO = flowInstanceMappingRepository.selectFlowInstanceMapping(runtimeContext.getProjectId(), runtimeContext.getFlowInstanceId(), currentNodeInstance.getNodeInstanceId());
    flowInstanceMappingRepository.updateType(runtimeContext.getProjectId(), oldFlowInstanceMappingPO.getFlowInstanceId(), oldFlowInstanceMappingPO.getNodeInstanceId(), FlowInstanceMappingType.TERMINATED);

    FlowInstanceMapping newFlowInstanceMappingPO = JsonUtils.getInstance().convertValue(oldFlowInstanceMappingPO, FlowInstanceMapping.class);
    newFlowInstanceMappingPO.setId(null);
    newFlowInstanceMappingPO.setProjectId(runtimeContext.getProjectId());
    newFlowInstanceMappingPO.setNodeInstanceId(newNodeInstanceId);
    newFlowInstanceMappingPO.setCreateTime(LocalDateTime.now());
    newFlowInstanceMappingPO.setModifyTime(LocalDateTime.now());
    flowInstanceMappingRepository.insert(newFlowInstanceMappingPO);
  }

  /**
   * common handle RuntimeResult from startProcessCallActivity, commitCallActivity, rollbackCallActivity.
   *
   * @param runtimeContext
   * @param runtimeResult
   * @throws ProcessException
   */
  protected void handleCallActivityResult(RuntimeContext runtimeContext, RuntimeResult runtimeResult) throws ProcessException {
    ErrorEnum errorEnum = ErrorEnum.getErrorEnum(runtimeResult.getErrCode());
    switch (errorEnum) {
      case SUCCESS:
        handleSuccessSubFlowResult(runtimeContext, runtimeResult);
        break;
      case COMMIT_SUSPEND:
      case ROLLBACK_SUSPEND:
        runtimeContext.getCurrentNodeInstance().setStatus(NodeInstanceStatus.ACTIVE);
        runtimeContext.setCallActivityRuntimeResultList(Arrays.asList(runtimeResult));
        throw new SuspendException(errorEnum);
      default:
        throw new ProcessException(errorEnum);
    }
  }

  private void handleSuccessSubFlowResult(RuntimeContext runtimeContext, RuntimeResult runtimeResult) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    if (runtimeResult.getStatus() == FlowInstanceStatus.TERMINATED) {
      // The subFlow rollback from the StartNode to the MainFlow
      currentNodeInstance.setStatus(NodeInstanceStatus.DISABLED);
      flowInstanceMappingRepository.updateType(runtimeContext.getProjectId(), runtimeContext.getFlowInstanceId(), currentNodeInstance.getNodeInstanceId(), FlowInstanceMappingType.TERMINATED);
    } else if (runtimeResult.getStatus() == FlowInstanceStatus.END) {
      // The subFlow is completed from the EndNode to the MainFlow
      currentNodeInstance.setStatus(NodeInstanceStatus.COMPLETED);
      // transfer data from subFlow to MainFlow
      saveCallActivityEndInstanceData(runtimeContext, runtimeResult);
    }
  }

  private void saveCallActivityEndInstanceData(RuntimeContext runtimeContext, RuntimeResult runtimeResult) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    Map<String, Object> instanceDataFromSubFlow = calculateCallActivityOutParamFromSubFlow(runtimeContext, runtimeResult.getVariables());
    // 1.merge to current data
    Map<String, Object> currentInstanceDataMap = runtimeContext.getInstanceDataMap();
    currentInstanceDataMap.putAll(instanceDataFromSubFlow);
    // 2.save data
    String instanceDataId = genId();
    dev.flexmodel.codegen.entity.InstanceData instanceDataPO = buildCallActivityEndInstanceData(instanceDataId, runtimeContext);
    instanceDataRepository.insert(instanceDataPO);
    runtimeContext.setInstanceDataId(instanceDataId);
    // 3.set currentNode completed
    currentNodeInstance.setInstanceDataId(runtimeContext.getInstanceDataId());
  }
}
