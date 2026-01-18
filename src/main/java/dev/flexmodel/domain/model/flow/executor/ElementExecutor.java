package dev.flexmodel.domain.model.flow.executor;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.codegen.entity.NodeInstance;
import dev.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.exception.ReentrantException;
import dev.flexmodel.domain.model.flow.exception.SuspendException;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.domain.model.flow.shared.common.FlowElementType;
import dev.flexmodel.domain.model.flow.shared.common.NodeInstanceStatus;
import dev.flexmodel.domain.model.flow.shared.common.RuntimeContext;
import dev.flexmodel.domain.model.flow.shared.util.ExpressionCalculator;
import dev.flexmodel.domain.model.flow.shared.util.FlowModelUtil;
import dev.flexmodel.domain.model.flow.shared.util.InstanceDataUtil;
import dev.flexmodel.shared.utils.JsonUtils;
import dev.flexmodel.shared.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class ElementExecutor extends RuntimeExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElementExecutor.class);

  @Inject
  protected ExpressionCalculator expressionCalculator;

  @Inject
  protected Instance<ExecutorFactory> executorFactoryInstance;

  @Override
  public void execute(RuntimeContext runtimeContext) throws ProcessException {
    try {
      preExecute(runtimeContext);
      doExecute(runtimeContext);
    } catch (ReentrantException re) {
      LOGGER.warn("execute ReentrantException: reentrant execute.||runtimeContext={},", runtimeContext, re);
    } catch (SuspendException se) {
      LOGGER.info("execute suspend.||runtimeContext={}", runtimeContext);
      throw se;
    } finally {
      postExecute(runtimeContext);
    }
  }

  /**
   * Init runtimeContext: update currentNodeInstance
   * 1.currentNodeInfo(nodeInstance & nodeKey): currentNode is this.model
   * 2.sourceNodeInfo(nodeInstance & nodeKey): sourceNode is runtimeContext.currentNodeInstance
   */
  protected void preExecute(RuntimeContext runtimeContext) throws ProcessException {

    NodeInstanceBO currentNodeInstance = new NodeInstanceBO();

    String flowInstanceId = runtimeContext.getFlowInstanceId();
    String nodeKey = runtimeContext.getCurrentNodeModel().getKey();

    //get sourceInfo
    String sourceNodeInstanceId = "";
    String sourceNodeKey = "";
    NodeInstanceBO sourceNodeInstance = runtimeContext.getCurrentNodeInstance();
    if (sourceNodeInstance != null) {
      // TODO: 2019/12/30 cache
      NodeInstance nodeInstance = nodeInstanceRepository.selectBySourceInstanceId(runtimeContext.getProjectId(), flowInstanceId,
        sourceNodeInstance.getNodeInstanceId(), nodeKey);
      //reentrant check
      if (nodeInstance != null) {
        currentNodeInstance = JsonUtils.getInstance().convertValue(nodeInstance, NodeInstanceBO.class);
        runtimeContext.setCurrentNodeInstance(currentNodeInstance);
        LOGGER.warn("preExecute reentrant.||nodeInstancePO={}", nodeInstance);
        return;
      }
      sourceNodeInstanceId = sourceNodeInstance.getNodeInstanceId();
      sourceNodeKey = sourceNodeInstance.getNodeKey();
    }

    String nodeInstanceId = genId();
    currentNodeInstance.setNodeInstanceId(nodeInstanceId);
    currentNodeInstance.setNodeKey(nodeKey);
    currentNodeInstance.setSourceNodeInstanceId(sourceNodeInstanceId);
    currentNodeInstance.setSourceNodeKey(sourceNodeKey);
    currentNodeInstance.setStatus(NodeInstanceStatus.ACTIVE);
    FlowElement flowElement = FlowModelUtil.getFlowElement(runtimeContext.getFlowElementMap(), currentNodeInstance.getNodeKey());
    currentNodeInstance.getProperties().putAll(flowElement.getProperties());
    currentNodeInstance.getProperties().putAll(runtimeContext.getExtendProperties());
    currentNodeInstance.setNodeType(runtimeContext.getCurrentNodeModel().getType());
    currentNodeInstance.setInstanceDataId(Objects.toString(runtimeContext.getInstanceDataId(), ""));

    runtimeContext.setCurrentNodeInstance(currentNodeInstance);
  }

  protected void doExecute(RuntimeContext runtimeContext) throws ProcessException {
  }

  protected void postExecute(RuntimeContext runtimeContext) throws ProcessException {
  }

  @Override
  protected RuntimeExecutor getExecuteExecutor(RuntimeContext runtimeContext) throws ProcessException {
    Map<String, FlowElement> flowElementMap = runtimeContext.getFlowElementMap();
    FlowElement flowElement = getUniqueNextNode(runtimeContext.getCurrentNodeModel(), flowElementMap);
    runtimeContext.setCurrentNodeModel(flowElement);
    return executorFactoryInstance.get().getElementExecutor(flowElement);
  }

  @Override
  public void commit(RuntimeContext runtimeContext) throws ProcessException {
    preCommit(runtimeContext);

    try {
      doCommit(runtimeContext);
    } catch (SuspendException se) {
      LOGGER.warn("SuspendException.");
      throw se;
    } finally {
      postCommit(runtimeContext);
    }
  }


  protected void preCommit(RuntimeContext runtimeContext) throws ProcessException {
    LOGGER.warn("preCommit: unsupported element type.||flowInstanceId={}||elementType={}",
      runtimeContext.getFlowInstanceId(), runtimeContext.getCurrentNodeModel().getType());
    throw new ProcessException(ErrorEnum.UNSUPPORTED_ELEMENT_TYPE);
  }

  protected void doCommit(RuntimeContext runtimeContext) throws ProcessException {
  }

  protected void postCommit(RuntimeContext runtimeContext) throws ProcessException {
  }

  @Override
  public void rollback(RuntimeContext runtimeContext) throws ProcessException {
    try {
      preRollback(runtimeContext);
      doRollback(runtimeContext);
    } catch (SuspendException se) {
      LOGGER.warn("SuspendException.");
      throw se;
    } catch (ReentrantException re) {
      LOGGER.warn("ReentrantException: reentrant rollback.");
    } finally {
      postRollback(runtimeContext);
    }
  }

  /**
   * Init runtimeContext: update currentNodeInstance
   * <p>
   * Case1. First node(UserTask) to rollback(there's no currentNodeInstance in runtimeContext):
   * Set newCurrentNodeInstance = suspendNodeInstance
   * <p>
   * Case2. Un-first node to rollback:
   * Set newCurrentNodeInstance = oldCurrentNodeInstance.sourceNodeInstance
   * <p>
   * ReentrantException: while currentNodeInstance is DISABLED
   *
   * @throws Exception
   */
  protected void preRollback(RuntimeContext runtimeContext) throws ProcessException {
    String flowInstanceId = runtimeContext.getFlowInstanceId();
    String nodeInstanceId, nodeKey;
    NodeInstanceBO currentNodeInstance;
    if (runtimeContext.getCurrentNodeInstance() == null) {
      //case1
      currentNodeInstance = runtimeContext.getSuspendNodeInstance();
    } else {
      //case2
      nodeInstanceId = runtimeContext.getCurrentNodeInstance().getSourceNodeInstanceId();
      NodeInstance currentNodeInstancePO = nodeInstanceRepository.selectByNodeInstanceId(runtimeContext.getProjectId(), flowInstanceId, nodeInstanceId);
      if (currentNodeInstancePO == null) {
        LOGGER.warn("preRollback failed: cannot find currentNodeInstancePO from db."
                    + "||flowInstanceId={}||nodeInstanceId={}", flowInstanceId, nodeInstanceId);
        throw new ProcessException(ErrorEnum.GET_NODE_INSTANCE_FAILED);
      }
      currentNodeInstance = JsonUtils.getInstance().convertValue(currentNodeInstancePO, NodeInstanceBO.class);

      String currentInstanceDataId = currentNodeInstance.getInstanceDataId();
      runtimeContext.setInstanceDataId(currentInstanceDataId);
      dev.flexmodel.codegen.entity.InstanceData instanceData = instanceDataRepository.select(runtimeContext.getProjectId(), flowInstanceId, currentInstanceDataId);
      Map<String, Object> currentInstanceDataMap = InstanceDataUtil.getInstanceDataMap(instanceData.getInstanceData());
      runtimeContext.setInstanceDataMap(currentInstanceDataMap);
    }
    runtimeContext.setCurrentNodeInstance(currentNodeInstance);

    nodeInstanceId = currentNodeInstance.getNodeInstanceId();
    nodeKey = currentNodeInstance.getNodeKey();
    int currentStatus = currentNodeInstance.getStatus();
    if (currentStatus == NodeInstanceStatus.DISABLED) {
      LOGGER.warn("preRollback: reentrant process.||flowInstanceId={}||nodeInstance={}||nodeKey={}", flowInstanceId, nodeInstanceId, nodeKey);
      throw new ReentrantException(ErrorEnum.REENTRANT_WARNING);
    }
    LOGGER.info("preRollback done.||flowInstanceId={}||nodeInstance={}||nodeKey={}", flowInstanceId, nodeInstanceId, nodeKey);
  }

  /**
   * Common rollback: overwrite it in customized elementExecutor or do nothing
   *
   * @throws Exception
   */
  protected void doRollback(RuntimeContext runtimeContext) throws ProcessException {
  }

  /**
   * Update runtimeContext: update currentNodeInstance.status to DISABLED and add it to nodeInstanceList
   *
   * @throws Exception
   */
  protected void postRollback(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    currentNodeInstance.setStatus(NodeInstanceStatus.DISABLED);
    runtimeContext.getNodeInstanceList().add(currentNodeInstance);
  }

  /**
   * Get elementExecutor to rollback:
   * Get sourceNodeInstanceId from currentNodeInstance and get sourceElement
   *
   * @return
   * @throws Exception
   */
  @Override
  protected ElementExecutor getRollbackExecutor(RuntimeContext runtimeContext) throws ProcessException {
    String flowInstanceId = runtimeContext.getFlowInstanceId();
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();

    String sourceNodeInstanceId = currentNodeInstance.getSourceNodeInstanceId();
    if (StringUtils.isBlank(sourceNodeInstanceId)) {
      LOGGER.warn("getRollbackExecutor: there's no sourceNodeInstance(startEvent)."
                  + "||flowInstanceId={}||nodeInstanceId={}", flowInstanceId, currentNodeInstance.getNodeInstanceId());
      return null;
    }

    // TODO: 2019/12/13 get from cache
    dev.flexmodel.codegen.entity.NodeInstance sourceNodeInstancePO = nodeInstanceRepository.selectByNodeInstanceId(runtimeContext.getProjectId(), flowInstanceId, sourceNodeInstanceId);
    if (sourceNodeInstancePO == null) {
      LOGGER.warn("getRollbackExecutor failed: cannot find sourceNodeInstance from db."
                  + "||flowInstanceId={}||sourceNodeInstanceId={}", flowInstanceId, sourceNodeInstanceId);
      throw new ProcessException(ErrorEnum.GET_NODE_INSTANCE_FAILED);
    }

    FlowElement sourceNode = FlowModelUtil.getFlowElement(runtimeContext.getFlowElementMap(),
      sourceNodeInstancePO.getNodeKey());

    // TODO: 2019/12/18
    runtimeContext.setCurrentNodeModel(sourceNode);
    return executorFactoryInstance.get().getElementExecutor(sourceNode);
  }

  @Override
  protected boolean isCompleted(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO nodeInstance = runtimeContext.getCurrentNodeInstance();
    //case 1.startEvent
    if (nodeInstance == null) {
      return false;
    }

    //case 2.begin to process the node
    if (!runtimeContext.getCurrentNodeModel().getKey().equals(nodeInstance.getNodeKey())) {
      return false;
    }

    //case 3.process completed
    if (nodeInstance.getStatus() == NodeInstanceStatus.COMPLETED) {
      return true;
    }

    //case 4.to process
    return false;
  }

  protected FlowElement getUniqueNextNode(FlowElement currentFlowElement, Map<String, FlowElement> flowElementMap) {
    List<String> outgoingKeyList = currentFlowElement.getOutgoing();
    String nextElementKey = outgoingKeyList.get(0);
    FlowElement nextFlowElement = FlowModelUtil.getFlowElement(flowElementMap, nextElementKey);
    while (nextFlowElement.getType() == FlowElementType.SEQUENCE_FLOW) {
      nextFlowElement = getUniqueNextNode(nextFlowElement, flowElementMap);
    }
    return nextFlowElement;
  }

  protected FlowElement calculateNextNode(FlowElement currentFlowElement, Map<String, FlowElement> flowElementMap,
                                          Map<String, Object> instanceDataMap) throws ProcessException {
    FlowElement nextFlowElement = calculateOutgoing(currentFlowElement, flowElementMap, instanceDataMap);

    while (nextFlowElement.getType() == FlowElementType.SEQUENCE_FLOW) {
      nextFlowElement = getUniqueNextNode(nextFlowElement, flowElementMap);
    }
    return nextFlowElement;
  }

  private FlowElement calculateOutgoing(FlowElement flowElement, Map<String, FlowElement> flowElementMap,
                                        Map<String, Object> instanceDataMap) throws ProcessException {
    FlowElement defaultElement = null;

    List<String> outgoingList = flowElement.getOutgoing();
    for (String outgoingKey : outgoingList) {
      FlowElement outgoingSequenceFlow = FlowModelUtil.getFlowElement(flowElementMap, outgoingKey);

      //case1 condition is true, hit the outgoing
      String condition = FlowModelUtil.getConditionFromSequenceFlow(outgoingSequenceFlow);
      if (StringUtils.isNotBlank(condition) && processCondition(condition, instanceDataMap)) {
        return outgoingSequenceFlow;
      }

      if (FlowModelUtil.isDefaultCondition(outgoingSequenceFlow)) {
        defaultElement = outgoingSequenceFlow;
      }
    }
    //case2 return default while it has is configured
    if (defaultElement != null) {
      LOGGER.info("calculateOutgoing: return defaultElement.||nodeKey={}", flowElement.getKey());
      return defaultElement;
    }

    LOGGER.warn("calculateOutgoing failed.||nodeKey={}", flowElement.getKey());
    throw new ProcessException(ErrorEnum.GET_OUTGOING_FAILED);
  }

  protected boolean processCondition(String expression, Map<String, Object> instanceDataMap) throws ProcessException {
    return expressionCalculator.calculate(expression, instanceDataMap);
  }
}
