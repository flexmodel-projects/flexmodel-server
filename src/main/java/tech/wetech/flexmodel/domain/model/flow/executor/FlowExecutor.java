package tech.wetech.flexmodel.domain.model.flow.executor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.codegen.entity.FlowInstance;
import tech.wetech.flexmodel.codegen.entity.InstanceData;
import tech.wetech.flexmodel.codegen.entity.NodeInstance;
import tech.wetech.flexmodel.codegen.entity.NodeInstanceLog;
import tech.wetech.flexmodel.domain.model.flow.bo.NodeInstanceBO;
import tech.wetech.flexmodel.domain.model.flow.common.*;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;
import tech.wetech.flexmodel.domain.model.flow.exception.ReentrantException;
import tech.wetech.flexmodel.domain.model.flow.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.util.FlowModelUtil;
import tech.wetech.flexmodel.domain.model.flow.util.InstanceDataUtil;
import tech.wetech.flexmodel.shared.utils.CollectionUtils;
import tech.wetech.flexmodel.shared.utils.JsonUtils;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Singleton
public class FlowExecutor extends RuntimeExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutor.class);

  @Inject
  private FlowInstanceRepository processInstanceDAO;

  @Inject
  private Instance<ExecutorFactory> executorFactoryInstance;

  ////////////////////////////////////////execute////////////////////////////////////////

  @Override
  public void execute(RuntimeContext runtimeContext) throws ProcessException {
    int processStatus = ProcessStatus.SUCCESS;
    try {
      preExecute(runtimeContext);
      doExecute(runtimeContext);
    } catch (ProcessException pe) {
      if (!ErrorEnum.isSuccess(pe.getErrNo())) {
        processStatus = ProcessStatus.FAILED;
      }
      throw pe;
    } finally {
      runtimeContext.setProcessStatus(processStatus);
      postExecute(runtimeContext);
    }
  }

  /**
   * Fill runtimeContext:
   * 1. Generate flowInstanceId and insert FlowInstancePO into db
   * 2. Generate instanceDataId and insert InstanceDataPO into db
   * 3. Update runtimeContext: flowInstanceId, flowInstanceStatus, instanceDataId, nodeInstanceList, suspendNodeInstance
   *
   * @throws Exception
   */
  private void preExecute(RuntimeContext runtimeContext) throws ProcessException {
    //1.save FlowInstancePO into db
    FlowInstance flowInstancePO = saveFlowInstance(runtimeContext);

    //2.save InstanceDataPO into db
    String instanceDataId = saveInstanceData(flowInstancePO, runtimeContext.getInstanceDataMap());

    //3.update runtimeContext
    fillExecuteContext(runtimeContext, flowInstancePO.getFlowInstanceId(), instanceDataId);
  }

  private FlowInstance saveFlowInstance(RuntimeContext runtimeContext) throws ProcessException {
    FlowInstance flowInstancePO = buildFlowInstancePO(runtimeContext);
    int result = processInstanceDAO.insert(flowInstancePO);
    if (result == 1) {
      return flowInstancePO;
    }
    LOGGER.warn("saveFlowInstancePO: insert failed.||flowInstancePO={}", flowInstancePO);
    throw new ProcessException(ErrorEnum.SAVE_FLOW_INSTANCE_FAILED);
  }

  private FlowInstance buildFlowInstancePO(RuntimeContext runtimeContext) {
    FlowInstance flowInstancePO = JsonUtils.getInstance().convertValue(runtimeContext, FlowInstance.class);
    // generate flowInstanceId
    flowInstancePO.setFlowInstanceId(genId());
    RuntimeContext parentRuntimeContext = runtimeContext.getParentRuntimeContext();
    if (parentRuntimeContext != null) {
      flowInstancePO.setParentFlowInstanceId(parentRuntimeContext.getFlowInstanceId());
    }
    flowInstancePO.setStatus(FlowInstanceStatus.RUNNING);
    flowInstancePO.setCreateTime(LocalDateTime.now());
    flowInstancePO.setModifyTime(LocalDateTime.now());
    return flowInstancePO;
  }

  private String saveInstanceData(FlowInstance flowInstancePO, Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataMap) throws ProcessException {
    if (instanceDataMap == null || instanceDataMap.isEmpty()) {
      return "";
    }

    InstanceData instanceDataPO = buildInstanceDataPO(flowInstancePO, instanceDataMap);
    int result = instanceDataRepository.insert(instanceDataPO);
    if (result == 1) {
      return instanceDataPO.getInstanceDataId();
    }

    LOGGER.warn("saveInstanceDataPO: insert failed.||instanceDataPO={}", instanceDataPO);
    throw new ProcessException(ErrorEnum.SAVE_INSTANCE_DATA_FAILED);
  }

  private InstanceData buildInstanceDataPO(FlowInstance flowInstancePO, Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataMap) {
    InstanceData instanceDataPO = JsonUtils.getInstance().convertValue(flowInstancePO, InstanceData.class);
    // fix primary key duplicated
    instanceDataPO.setId(null);

    // generate instanceDataId
    instanceDataPO.setInstanceDataId(genId());
    instanceDataPO.setInstanceData(InstanceDataUtil.getInstanceDataListStr(instanceDataMap));

    instanceDataPO.setNodeInstanceId("");
    instanceDataPO.setNodeKey("");
    instanceDataPO.setCreateTime(LocalDateTime.now());
    instanceDataPO.setType(InstanceDataType.INIT);
    return instanceDataPO;
  }

  private void fillExecuteContext(RuntimeContext runtimeContext, String flowInstanceId, String instanceDataId) throws ProcessException {
    runtimeContext.setFlowInstanceId(flowInstanceId);
    runtimeContext.setFlowInstanceStatus(FlowInstanceStatus.RUNNING);

    runtimeContext.setInstanceDataId(instanceDataId);

    runtimeContext.setNodeInstanceList(Lists.newArrayList());

    //set startEvent into suspendNodeInstance as the first node to process
    Map<String, FlowElement> flowElementMap = runtimeContext.getFlowElementMap();
    FlowElement startEvent = FlowModelUtil.getStartEvent(flowElementMap);
    if (startEvent == null) {
      LOGGER.warn("fillExecuteContext failed: cannot get startEvent.||flowInstance={}||flowDeployId={}",
        runtimeContext.getFlowInstanceId(), runtimeContext.getFlowDeployId());
      throw new ProcessException(ErrorEnum.GET_NODE_FAILED);
    }
    NodeInstanceBO suspendNodeInstance = new NodeInstanceBO();
    suspendNodeInstance.setNodeKey(startEvent.getKey());
    suspendNodeInstance.setNodeType(startEvent.getType());
    suspendNodeInstance.setStatus(NodeInstanceStatus.ACTIVE);
    suspendNodeInstance.setSourceNodeInstanceId("");
    suspendNodeInstance.setSourceNodeKey("");
    runtimeContext.setSuspendNodeInstance(suspendNodeInstance);

    runtimeContext.setCurrentNodeModel(startEvent);
  }

  private void doExecute(RuntimeContext runtimeContext) throws ProcessException {
    RuntimeExecutor runtimeExecutor = getExecuteExecutor(runtimeContext);
    while (runtimeExecutor != null) {
      runtimeExecutor.execute(runtimeContext);
      runtimeExecutor = runtimeExecutor.getExecuteExecutor(runtimeContext);
    }
  }

  private void postExecute(RuntimeContext runtimeContext) throws ProcessException {

    //1.update context with processStatus
    if (runtimeContext.getProcessStatus() == ProcessStatus.SUCCESS) {
      //SUCCESS: update runtimeContext: update suspendNodeInstance
      if (runtimeContext.getCurrentNodeInstance() != null) {
        runtimeContext.setSuspendNodeInstance(runtimeContext.getCurrentNodeInstance());
      }
    }

    //2.save nodeInstanceList to db
    saveNodeInstanceList(runtimeContext, NodeInstanceType.EXECUTE);

    //3.update flowInstance status while completed
    if (isCompleted(runtimeContext)) {
      if (isSubFlowInstance(runtimeContext)) {
        processInstanceDAO.updateStatus(runtimeContext.getFlowInstanceId(), FlowInstanceStatus.END);
        runtimeContext.setFlowInstanceStatus(FlowInstanceStatus.END);
      } else {
        processInstanceDAO.updateStatus(runtimeContext.getFlowInstanceId(), FlowInstanceStatus.COMPLETED);
        runtimeContext.setFlowInstanceStatus(FlowInstanceStatus.COMPLETED);
      }
      LOGGER.info("postExecute: flowInstance process completely.||flowInstanceId={}", runtimeContext.getFlowInstanceId());
    }
  }


  ////////////////////////////////////////commit////////////////////////////////////////

  @Override
  public void commit(RuntimeContext runtimeContext) throws ProcessException {
    int processStatus = ProcessStatus.SUCCESS;
    try {
      preCommit(runtimeContext);
      doCommit(runtimeContext);
    } catch (ReentrantException re) {
      //ignore
    } catch (ProcessException pe) {
      if (!ErrorEnum.isSuccess(pe.getErrNo())) {
        processStatus = ProcessStatus.FAILED;
      }
      throw pe;
    } finally {
      runtimeContext.setProcessStatus(processStatus);
      postCommit(runtimeContext);
    }
  }

  /**
   * Fill runtimeContext:
   * 1. Get instanceData from db firstly
   * 2. merge and save instanceData while commitData is not empty
   * 3. Update runtimeContext: instanceDataId, instanceDataMap, nodeInstanceList, suspendNodeInstance
   *
   * @throws Exception
   */
  private void preCommit(RuntimeContext runtimeContext) throws ProcessException {
    String flowInstanceId = runtimeContext.getFlowInstanceId();
    NodeInstanceBO suspendNodeInstance = runtimeContext.getSuspendNodeInstance();
    String nodeInstanceId = suspendNodeInstance.getNodeInstanceId();

    //1.get instanceData from db
    NodeInstance nodeInstancePO = nodeInstanceRepository.selectByNodeInstanceId(flowInstanceId, nodeInstanceId);
    if (nodeInstancePO == null) {
      LOGGER.warn("preCommit failed: cannot find nodeInstancePO from db.||flowInstanceId={}||nodeInstanceId={}",
        flowInstanceId, nodeInstanceId);
      throw new ProcessException(ErrorEnum.GET_NODE_INSTANCE_FAILED);
    }

    //unexpected: flowInstance is completed
    if (isCompleted(runtimeContext)) {
      LOGGER.warn("preExecute warning: reentrant process. FlowInstance has been processed completely.||runtimeContext={}", runtimeContext);
      runtimeContext.setFlowInstanceStatus(FlowInstanceStatus.COMPLETED);
      suspendNodeInstance.setId(nodeInstancePO.getId());
      suspendNodeInstance.setNodeKey(nodeInstancePO.getNodeKey());
      suspendNodeInstance.setNodeType(nodeInstancePO.getNodeType());
      suspendNodeInstance.setSourceNodeInstanceId(nodeInstancePO.getSourceNodeInstanceId());
      suspendNodeInstance.setSourceNodeKey(nodeInstancePO.getSourceNodeKey());
      suspendNodeInstance.setInstanceDataId(nodeInstancePO.getInstanceDataId());
      suspendNodeInstance.setStatus(nodeInstancePO.getStatus());
      throw new ReentrantException(ErrorEnum.REENTRANT_WARNING);
    }
    Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataMap;
    String instanceDataId = nodeInstancePO.getInstanceDataId();
    if (StringUtils.isBlank(instanceDataId)) {
      instanceDataMap = Maps.newHashMap();
    } else {
      InstanceData instanceDataPO = instanceDataRepository.select(flowInstanceId, instanceDataId);
      if (instanceDataPO == null) {
        LOGGER.warn("preCommit failed: cannot find instanceDataPO from db." +
                    "||flowInstanceId={}||instanceDataId={}", flowInstanceId, instanceDataId);
        throw new ProcessException(ErrorEnum.GET_INSTANCE_DATA_FAILED);
      }
      instanceDataMap = InstanceDataUtil.getInstanceDataMap(instanceDataPO.getInstanceData());
    }

    //2.merge data while commitDataMap is not empty
    Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> commitDataMap = runtimeContext.getInstanceDataMap();
    boolean isCallActivityNode = FlowModelUtil.isElementType(nodeInstancePO.getNodeKey(), runtimeContext.getFlowElementMap(), FlowElementType.CALL_ACTIVITY);
    if (isCallActivityNode) {
      // commit callActivity not allow merge data
      instanceDataMap = commitDataMap;
    } else if (commitDataMap != null && !commitDataMap.isEmpty()) {
      instanceDataId = genId();
      instanceDataMap.putAll(commitDataMap);

      InstanceData commitInstanceDataPO = buildCommitInstanceData(runtimeContext, nodeInstanceId,
        nodeInstancePO.getNodeKey(), instanceDataId, instanceDataMap);
      instanceDataRepository.insert(commitInstanceDataPO);
    }

    //3.update runtimeContext
    fillCommitContext(runtimeContext, nodeInstancePO, instanceDataId, instanceDataMap);
  }

  private InstanceData buildCommitInstanceData(RuntimeContext runtimeContext, String nodeInstanceId, String nodeKey,
                                               String newInstanceDataId, Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataMap) {
    InstanceData instanceDataPO = JsonUtils.getInstance().convertValue(runtimeContext, InstanceData.class);

    instanceDataPO.setNodeInstanceId(nodeInstanceId);
    instanceDataPO.setNodeKey(nodeKey);
    instanceDataPO.setType(InstanceDataType.COMMIT);
    instanceDataPO.setCreateTime(LocalDateTime.now());

    instanceDataPO.setInstanceDataId(newInstanceDataId);
    instanceDataPO.setInstanceData(InstanceDataUtil.getInstanceDataListStr(instanceDataMap));

    return instanceDataPO;
  }

  private void fillCommitContext(RuntimeContext runtimeContext, NodeInstance nodeInstancePO, String instanceDataId,
                                 Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataMap) throws ProcessException {

    runtimeContext.setInstanceDataId(instanceDataId);
    runtimeContext.setInstanceDataMap(instanceDataMap);

    updateSuspendNodeInstanceBO(runtimeContext.getSuspendNodeInstance(), nodeInstancePO, instanceDataId);

    setCurrentFlowModel(runtimeContext);

    runtimeContext.setNodeInstanceList(Lists.newArrayList());
  }

  private void doCommit(RuntimeContext runtimeContext) throws ProcessException {
    RuntimeExecutor runtimeExecutor = getExecuteExecutor(runtimeContext);
    runtimeExecutor.commit(runtimeContext);

    runtimeExecutor = runtimeExecutor.getExecuteExecutor(runtimeContext);
    while (runtimeExecutor != null) {
      runtimeExecutor.execute(runtimeContext);
      runtimeExecutor = runtimeExecutor.getExecuteExecutor(runtimeContext);
    }
  }

  private void postCommit(RuntimeContext runtimeContext) throws ProcessException {
    if (runtimeContext.getProcessStatus() == ProcessStatus.SUCCESS && runtimeContext.getCurrentNodeInstance() != null) {
      runtimeContext.setSuspendNodeInstance(runtimeContext.getCurrentNodeInstance());
    }
    //update FlowInstancePO to db
    saveNodeInstanceList(runtimeContext, NodeInstanceType.COMMIT);

    if (isCompleted(runtimeContext)) {
      if (isSubFlowInstance(runtimeContext)) {
        processInstanceDAO.updateStatus(runtimeContext.getFlowInstanceId(), FlowInstanceStatus.END);
        runtimeContext.setFlowInstanceStatus(FlowInstanceStatus.END);
      } else {
        processInstanceDAO.updateStatus(runtimeContext.getFlowInstanceId(), FlowInstanceStatus.COMPLETED);
        runtimeContext.setFlowInstanceStatus(FlowInstanceStatus.COMPLETED);
      }

      LOGGER.info("postCommit: flowInstance process completely.||flowInstanceId={}", runtimeContext.getFlowInstanceId());
    }
  }

  ////////////////////////////////////////rollback////////////////////////////////////////

  @Override
  public void rollback(RuntimeContext runtimeContext) throws ProcessException {
    int processStatus = ProcessStatus.SUCCESS;
    try {
      preRollback(runtimeContext);
      doRollback(runtimeContext);
    } catch (ReentrantException re) {
      //ignore
    } catch (ProcessException pe) {
      if (!ErrorEnum.isSuccess(pe.getErrNo())) {
        processStatus = ProcessStatus.FAILED;
      }
      throw pe;
    } finally {
      runtimeContext.setProcessStatus(processStatus);
      postRollback(runtimeContext);
    }
  }

  private void preRollback(RuntimeContext runtimeContext) throws ProcessException {
    String flowInstanceId = runtimeContext.getFlowInstanceId();

    //1.check node: only the latest enabled(ACTIVE or COMPLETED) nodeInstance can be rollbacked.
    String suspendNodeInstanceId = runtimeContext.getSuspendNodeInstance().getNodeInstanceId();
    NodeInstance rollbackNodeInstance = getActiveNodeForRollback(flowInstanceId, suspendNodeInstanceId,
      runtimeContext.getFlowElementMap());
    if (rollbackNodeInstance == null) {
      LOGGER.warn("preRollback failed: cannot rollback.||runtimeContext={}", runtimeContext);
      throw new ProcessException(ErrorEnum.ROLLBACK_FAILED);
    }

    //2.check status: flowInstance is completed
    if (isCompleted(runtimeContext)) {
      LOGGER.warn("invalid preRollback: FlowInstance has been processed completely."
                  + "||flowInstanceId={}||flowDeployId={}", flowInstanceId, runtimeContext.getFlowDeployId());
      NodeInstanceBO suspendNodeInstance = JsonUtils.getInstance().convertValue(rollbackNodeInstance, NodeInstanceBO.class);
      runtimeContext.setSuspendNodeInstance(suspendNodeInstance);
      runtimeContext.setFlowInstanceStatus(FlowInstanceStatus.COMPLETED);
      throw new ProcessException(ErrorEnum.ROLLBACK_FAILED);
    }

    //3.get instanceData
    String instanceDataId = rollbackNodeInstance.getInstanceDataId();
    Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataMap;
    if (StringUtils.isBlank(instanceDataId)) {
      instanceDataMap = Maps.newHashMap();
    } else {
      InstanceData instanceDataPO = instanceDataRepository.select(flowInstanceId, instanceDataId);
      if (instanceDataPO == null) {
        LOGGER.warn("preRollback failed: cannot find instanceDataPO from db."
                    + "||flowInstanceId={}||instanceDataId={}", flowInstanceId, instanceDataId);
        throw new ProcessException(ErrorEnum.GET_INSTANCE_DATA_FAILED);
      }
      instanceDataMap = InstanceDataUtil.getInstanceDataMap(instanceDataPO.getInstanceData());
    }

    //4.update runtimeContext
    fillRollbackContext(runtimeContext, rollbackNodeInstance, instanceDataMap);
  }

  // if(canRollback): only the active Node or the lasted completed Node can be rollback
  private NodeInstance getActiveNodeForRollback(String flowInstanceId, String suspendNodeInstanceId,
                                                Map<String, FlowElement> flowElementMap) {
    List<NodeInstance> nodeInstancePOList = nodeInstanceRepository.selectDescByFlowInstanceId(flowInstanceId);
    if (CollectionUtils.isEmpty(nodeInstancePOList)) {
      LOGGER.warn("getActiveNodeForRollback: nodeInstancePOList is empty."
                  + "||flowInstanceId={}||suspendNodeInstanceId={}", flowInstanceId, suspendNodeInstanceId);
      return null;
    }

    for (NodeInstance nodeInstancePO : nodeInstancePOList) {
      int elementType = FlowModelUtil.getElementType(nodeInstancePO.getNodeKey(), flowElementMap);
      if (elementType != FlowElementType.USER_TASK
          && elementType != FlowElementType.END_EVENT
          && elementType != FlowElementType.CALL_ACTIVITY) {
        LOGGER.info("getActiveNodeForRollback: ignore un-userTask or un-endEvent or un-callActivity nodeInstance.||flowInstanceId={}"
                    + "||suspendNodeInstanceId={}||nodeKey={}", flowInstanceId, suspendNodeInstanceId, nodeInstancePO.getNodeKey());
        continue;
      }

      if (nodeInstancePO.getStatus() == NodeInstanceStatus.ACTIVE) {
        if (nodeInstancePO.getNodeInstanceId().equals(suspendNodeInstanceId)) {
          LOGGER.info("getActiveNodeForRollback: roll back the active Node."
                      + "||flowInstanceId={}||suspendNodeInstanceId={}", flowInstanceId, suspendNodeInstanceId);
          return nodeInstancePO;
        }
      } else if (nodeInstancePO.getStatus() == NodeInstanceStatus.COMPLETED) {
        if (nodeInstancePO.getNodeInstanceId().equals(suspendNodeInstanceId)) {
          LOGGER.info("getActiveNodeForRollback: roll back the lasted completed Node."
                      + "||flowInstanceId={}||suspendNodeInstanceId={}||activeNodeInstanceId={}",
            flowInstanceId, suspendNodeInstanceId, nodeInstancePO);
          return nodeInstancePO;
        }

        LOGGER.warn("getActiveNodeForRollback: cannot rollback the Node."
                    + "||flowInstanceId={}||suspendNodeInstanceId={}", flowInstanceId, suspendNodeInstanceId);
        return null;
      }
      LOGGER.info("getActiveNodeForRollback: ignore disabled Node instance.||flowInstanceId={}"
                  + "||suspendNodeInstanceId={}||status={}", flowInstanceId, suspendNodeInstanceId, nodeInstancePO.getStatus());

    }
    LOGGER.warn("getActiveNodeForRollback: cannot rollback the suspendNodeInstance."
                + "||flowInstanceId={}||suspendNodeInstanceId={}", flowInstanceId, suspendNodeInstanceId);
    return null;
  }

  private void doRollback(RuntimeContext runtimeContext) throws ProcessException {
    RuntimeExecutor runtimeExecutor = getRollbackExecutor(runtimeContext);
    while (runtimeExecutor != null) {
      runtimeExecutor.rollback(runtimeContext);
      runtimeExecutor = runtimeExecutor.getRollbackExecutor(runtimeContext);
    }
  }

  private void postRollback(RuntimeContext runtimeContext) {

    if (runtimeContext.getProcessStatus() != ProcessStatus.SUCCESS) {
      LOGGER.warn("postRollback: ignore while process failed.||runtimeContext={}", runtimeContext);
      return;
    }
    if (runtimeContext.getCurrentNodeInstance() != null) {
      runtimeContext.setSuspendNodeInstance(runtimeContext.getCurrentNodeInstance());
    }

    //update FlowInstancePO to db
    saveNodeInstanceList(runtimeContext, NodeInstanceType.ROLLBACK);

    if (FlowModelUtil.isElementType(runtimeContext.getCurrentNodeModel().getKey(), runtimeContext.getFlowElementMap(), FlowElementType.START_EVENT)) {
      runtimeContext.setFlowInstanceStatus(FlowInstanceStatus.TERMINATED);
      processInstanceDAO.updateStatus(runtimeContext.getFlowInstanceId(), FlowInstanceStatus.TERMINATED);
    } else if (runtimeContext.getFlowInstanceStatus() == FlowInstanceStatus.END) {
      runtimeContext.setFlowInstanceStatus(FlowInstanceStatus.RUNNING);
      processInstanceDAO.updateStatus(runtimeContext.getFlowInstanceId(), FlowInstanceStatus.RUNNING);
    }
  }

  private void fillRollbackContext(RuntimeContext runtimeContext, NodeInstance nodeInstancePO,
                                   Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataMap) throws ProcessException {
    runtimeContext.setInstanceDataId(nodeInstancePO.getInstanceDataId());
    runtimeContext.setInstanceDataMap(instanceDataMap);
    runtimeContext.setNodeInstanceList(Lists.newArrayList());
    NodeInstanceBO suspendNodeInstanceBO = buildSuspendNodeInstanceBO(nodeInstancePO);
    runtimeContext.setSuspendNodeInstance(suspendNodeInstanceBO);
    setCurrentFlowModel(runtimeContext);
  }

  private NodeInstanceBO buildSuspendNodeInstanceBO(NodeInstance nodeInstancePO) {
    NodeInstanceBO suspendNodeInstanceBO = JsonUtils.getInstance().convertValue(nodeInstancePO, NodeInstanceBO.class);
    return suspendNodeInstanceBO;
  }

  private void updateSuspendNodeInstanceBO(NodeInstanceBO suspendNodeInstanceBO, NodeInstance nodeInstancePO, String
    instanceDataId) {
    suspendNodeInstanceBO.setId(nodeInstancePO.getId());
    suspendNodeInstanceBO.setNodeKey(nodeInstancePO.getNodeKey());
    suspendNodeInstanceBO.setStatus(nodeInstancePO.getStatus());
    suspendNodeInstanceBO.setSourceNodeInstanceId(nodeInstancePO.getSourceNodeInstanceId());
    suspendNodeInstanceBO.setSourceNodeKey(nodeInstancePO.getSourceNodeKey());
    suspendNodeInstanceBO.setInstanceDataId(instanceDataId);
  }

  //suspendNodeInstanceBO is necessary
  private void setCurrentFlowModel(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO suspendNodeInstanceBO = runtimeContext.getSuspendNodeInstance();
    FlowElement currentNodeModel = FlowModelUtil.getFlowElement(runtimeContext.getFlowElementMap(), suspendNodeInstanceBO.getNodeKey());
    if (currentNodeModel == null) {
      LOGGER.warn("setCurrentFlowModel failed: cannot get currentNodeModel.||flowInstance={}||flowDeployId={}||nodeKey={}",
        runtimeContext.getFlowInstanceId(), runtimeContext.getFlowDeployId(), suspendNodeInstanceBO.getNodeKey());
      throw new ProcessException(ErrorEnum.GET_NODE_FAILED);
    }
    runtimeContext.setCurrentNodeModel(currentNodeModel);
  }

  @Override
  protected boolean isCompleted(RuntimeContext runtimeContext) throws ProcessException {
    if (runtimeContext.getFlowInstanceStatus() == FlowInstanceStatus.COMPLETED) {
      return true;
    }
    if (runtimeContext.getFlowInstanceStatus() == FlowInstanceStatus.END) {
      return false;
    }
    NodeInstanceBO suspendNodeInstance = runtimeContext.getSuspendNodeInstance();
    if (suspendNodeInstance == null) {
      LOGGER.warn("suspendNodeInstance is null.||runtimeContext={}", runtimeContext);
      return false;
    }

    if (suspendNodeInstance.getStatus() != NodeInstanceStatus.COMPLETED) {
      return false;
    }

    String nodeKey = suspendNodeInstance.getNodeKey();
    Map<String, FlowElement> flowElementMap = runtimeContext.getFlowElementMap();
    if (FlowModelUtil.getFlowElement(flowElementMap, nodeKey).getType() == FlowElementType.END_EVENT) {
      return true;
    }
    return false;
  }

  @Override
  protected RuntimeExecutor getExecuteExecutor(RuntimeContext runtimeContext) throws ProcessException {
    return getElementExecutor(runtimeContext);
  }

  @Override
  protected RuntimeExecutor getRollbackExecutor(RuntimeContext runtimeContext) throws ProcessException {
    return getElementExecutor(runtimeContext);
  }

  private RuntimeExecutor getElementExecutor(RuntimeContext runtimeContext) throws ProcessException {
    //if process completed, return null
    if (isCompleted(runtimeContext)) {
      return null;
    }
    return executorFactoryInstance.get().getElementExecutor(runtimeContext.getCurrentNodeModel());
  }

  ////////////////////////////////////////common////////////////////////////////////////

  private void saveNodeInstanceList(RuntimeContext runtimeContext, int nodeInstanceType) {

    List<NodeInstanceBO> processNodeList = runtimeContext.getNodeInstanceList();

    if (CollectionUtils.isEmpty(processNodeList)) {
      LOGGER.warn("saveNodeInstanceList: processNodeList is empty,||flowInstanceId={}||nodeInstanceType={}",
        runtimeContext.getFlowInstanceId(), nodeInstanceType);
      return;
    }

    List<NodeInstance> nodeInstancePOList = Lists.newArrayList();
    List<NodeInstanceLog> nodeInstanceLogPOList = Lists.newArrayList();

    processNodeList.forEach(nodeInstanceBO -> {
      NodeInstance nodeInstancePO = buildNodeInstance(runtimeContext, nodeInstanceBO);
      if (nodeInstancePO != null) {
        nodeInstancePOList.add(nodeInstancePO);

        //build nodeInstance log
        NodeInstanceLog nodeInstanceLogPO = buildNodeInstanceLogPO(nodeInstancePO, nodeInstanceType);
        nodeInstanceLogPOList.add(nodeInstanceLogPO);
      }
    });
    nodeInstanceRepository.insertOrUpdateList(nodeInstancePOList);
    nodeInstanceLogRepository.insertList(nodeInstanceLogPOList);
  }

  private NodeInstance buildNodeInstance(RuntimeContext runtimeContext, NodeInstanceBO nodeInstanceBO) {
    if (runtimeContext.getProcessStatus() == ProcessStatus.FAILED) {
      //set status=FAILED unless it is origin processNodeInstance(suspendNodeInstance)
      if (nodeInstanceBO.getNodeKey().equals(runtimeContext.getSuspendNodeInstance().getNodeKey())) {
        //keep suspendNodeInstance's status while process failed.
        return null;
      }
      nodeInstanceBO.setStatus(NodeInstanceStatus.FAILED);
    }

    NodeInstance nodeInstancePO = JsonUtils.getInstance().convertValue(nodeInstanceBO, NodeInstance.class);
    nodeInstancePO.setFlowInstanceId(runtimeContext.getFlowInstanceId());
    nodeInstancePO.setFlowDeployId(runtimeContext.getFlowDeployId());
    nodeInstancePO.setTenant(runtimeContext.getTenant());
    nodeInstancePO.setCaller(runtimeContext.getCaller());
    LocalDateTime currentTime = LocalDateTime.now();
    nodeInstancePO.setCreateTime(currentTime);
    nodeInstancePO.setModifyTime(currentTime);
    return nodeInstancePO;
  }

  private NodeInstanceLog buildNodeInstanceLogPO(NodeInstance nodeInstancePO, int nodeInstanceType) {
    NodeInstanceLog nodeInstanceLogPO = JsonUtils.getInstance().convertValue(nodeInstancePO, NodeInstanceLog.class);
    nodeInstanceLogPO.setId(null);
    nodeInstanceLogPO.setType(nodeInstanceType);
    return nodeInstanceLogPO;
  }

}
