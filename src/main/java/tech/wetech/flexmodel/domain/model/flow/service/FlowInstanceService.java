package tech.wetech.flexmodel.domain.model.flow.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.codegen.entity.FlowInstance;
import tech.wetech.flexmodel.codegen.entity.FlowInstanceMapping;
import tech.wetech.flexmodel.codegen.entity.NodeInstance;
import tech.wetech.flexmodel.domain.model.flow.dto.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowInstanceMappingRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.NodeInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.shared.common.FlowElementType;
import tech.wetech.flexmodel.domain.model.flow.shared.util.FlowModelUtil;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.shared.utils.CollectionUtils;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.util.*;

@Singleton
public class FlowInstanceService {

  protected static final Logger LOGGER = LoggerFactory.getLogger(FlowInstanceService.class);

  @Inject
  NodeInstanceRepository nodeInstanceRepository;

  @Inject
  FlowInstanceMappingRepository flowInstanceMappingRepository;

  @Inject
  FlowInstanceRepository flowInstanceRepository;

  @Inject
  FlowDeploymentRepository flowDeploymentRepository;

  /**
   * According to rootFlowInstanceId and commitNodeInstanceId, build and return NodeInstance stack.
   * When the subProcessInstance of each layer is executed, stack needs to pop up.
   * <p>
   * e.g.
   * <p>
   * rootNodeInstanceId
   * ^
   * ..................
   * ^
   * commitNodeInstanceId
   *
   * @param rootFlowInstanceId
   * @param commitNodeInstanceId
   * @return
   */
  public Stack<String> getNodeInstanceIdStack(String rootFlowInstanceId, String commitNodeInstanceId) {
    if (StringUtils.isBlank(commitNodeInstanceId)) {
      LOGGER.info("getNodeInstanceId2RootStack result is empty.||rootFlowInstanceId={}||commitNodeInstanceId={}", rootFlowInstanceId, commitNodeInstanceId);
      return new Stack<>();
    }
    FlowInstanceTreeResult flowInstanceTreeResult = buildFlowInstanceTree(rootFlowInstanceId,
      nodeInstancePO -> nodeInstancePO.getNodeInstanceId().equals(commitNodeInstanceId));
    NodeInstancePOJO rightNodeInstance = flowInstanceTreeResult.getInterruptNodeInstancePOJO();
    Stack<String> stack = new Stack<>();
    while (rightNodeInstance != null) {
      stack.push(rightNodeInstance.getId());
      rightNodeInstance = rightNodeInstance.getFlowInstance().getBelongNodeInstance();
    }
    LOGGER.info("getNodeInstanceId2RootStack result.||rootFlowInstanceId={}||commitNodeInstanceId={}||result={}", rootFlowInstanceId, commitNodeInstanceId, stack);
    return stack;
  }

  /**
   * According to rootFlowInstanceId, get all subFlowInstanceIds from db.
   *
   * @param rootFlowInstanceId
   * @return
   */
  public Set<String> getAllSubFlowInstanceIds(String rootFlowInstanceId) {
    FlowInstanceTreeResult flowInstanceTreeResult = buildFlowInstanceTree(rootFlowInstanceId, null);
    FlowInstancePOJO flowInstancePOJO = flowInstanceTreeResult.getRootFlowInstancePOJO();
    Set<String> result = getAllSubFlowInstanceIdsInternal(flowInstancePOJO);
    result.remove(rootFlowInstanceId);
    LOGGER.info("getAllSubFlowInstanceIds result.||rootFlowInstanceId={}||result={}", rootFlowInstanceId, result);
    return result;
  }

  private Set<String> getAllSubFlowInstanceIdsInternal(FlowInstancePOJO flowInstancePOJO) {
    Set<String> result = new TreeSet<>();
    if (flowInstancePOJO == null) {
      return result;
    }
    result.add(flowInstancePOJO.getId());
    List<NodeInstancePOJO> nodeInstanceList = flowInstancePOJO.getNodeInstanceList();
    for (NodeInstancePOJO nodeInstancePOJO : nodeInstanceList) {
      if (CollectionUtils.isEmpty(nodeInstancePOJO.getSubFlowInstanceList())) {
        continue;
      }
      FlowInstancePOJO subFlowInstancePOJO = nodeInstancePOJO.getSubFlowInstanceList().get(0);
      Set<String> subFlowInstanceResult = getAllSubFlowInstanceIdsInternal(subFlowInstancePOJO);
      result.addAll(subFlowInstanceResult);
    }
    return result;
  }


  /**
   * According to rootFlowInstanceId and nodeInstanceId,
   * Return the FlowInstanceId where the nodeInstanceId is located.
   *
   * @param rootFlowInstanceId
   * @param nodeInstanceId
   * @return
   */
  public String getFlowInstanceIdByRootFlowInstanceIdAndNodeInstanceId(String rootFlowInstanceId, String nodeInstanceId) {
    if (StringUtils.isBlank(nodeInstanceId)) {
      return "";
    }
    FlowInstanceTreeResult flowInstanceTreeResult = buildFlowInstanceTree(rootFlowInstanceId,
      nodeInstancePO -> nodeInstancePO.getNodeInstanceId().equals(nodeInstanceId));
    NodeInstancePOJO rightNodeInstance = flowInstanceTreeResult.getInterruptNodeInstancePOJO();
    if (rightNodeInstance == null) {
      return "";
    }
    return rightNodeInstance.getFlowInstance().getId();
  }

  /**
   * According to rootFlowInstanceId and instanceDataId,
   * Return the FlowInstanceId where the instanceDataId is located.
   *
   * @param rootFlowInstanceId
   * @param instanceDataId
   * @return
   */
  public String getFlowInstanceIdByRootFlowInstanceIdAndInstanceDataId(String rootFlowInstanceId, String instanceDataId) {
    if (StringUtils.isBlank(instanceDataId)) {
      return "";
    }
    FlowInstanceTreeResult flowInstanceTreeResult = buildFlowInstanceTree(rootFlowInstanceId,
      nodeInstancePO -> nodeInstancePO.getInstanceDataId().equals(instanceDataId));
    NodeInstancePOJO rightNodeInstance = flowInstanceTreeResult.getInterruptNodeInstancePOJO();
    if (rightNodeInstance == null) {
      return "";
    }
    return rightNodeInstance.getFlowInstance().getId();
  }

  // common : build a flowInstanceAndNodeInstance tree
  private FlowInstanceTreeResult buildFlowInstanceTree(String rootFlowInstanceId, InterruptCondition interruptCondition) {
    FlowInstanceTreeResult flowInstanceTreeResult = new FlowInstanceTreeResult();
    FlowInstancePOJO flowInstance = new FlowInstancePOJO();
    flowInstance.setId(rootFlowInstanceId);
    flowInstanceTreeResult.setRootFlowInstancePOJO(flowInstance);

    FlowInstance rootFlowInstance = flowInstanceRepository.selectByFlowInstanceId(rootFlowInstanceId);
    FlowDeployment rootFlowDeployment = flowDeploymentRepository.findByDeployId(rootFlowInstance.getFlowDeployId());
    Map<String, FlowElement> rootFlowElementMap = FlowModelUtil.getFlowElementMap(rootFlowDeployment.getFlowModel());

    List<NodeInstance> nodeInstancePOList = nodeInstanceRepository.selectDescByFlowInstanceId(rootFlowInstanceId);
    for (NodeInstance nodeInstancePO : nodeInstancePOList) {
      NodeInstancePOJO nodeInstance = new NodeInstancePOJO();
      nodeInstance.setId(nodeInstancePO.getNodeInstanceId());
      nodeInstance.setFlowInstance(flowInstance);
      flowInstance.getNodeInstanceList().add(nodeInstance);

      if (interruptCondition != null && interruptCondition.match(nodeInstancePO)) {
        flowInstanceTreeResult.setInterruptNodeInstancePOJO(nodeInstance);
        return flowInstanceTreeResult;
      }

      int elementType = FlowModelUtil.getElementType(nodeInstancePO.getNodeKey(), rootFlowElementMap);
      if (elementType != FlowElementType.CALL_ACTIVITY) {
        continue;
      }
      List<FlowInstanceMapping> flowInstanceMappingPOS = flowInstanceMappingRepository.selectFlowInstanceMappingList(nodeInstancePO.getFlowInstanceId(), nodeInstancePO.getNodeInstanceId());
      for (FlowInstanceMapping flowInstanceMappingPO : flowInstanceMappingPOS) {
        FlowInstanceTreeResult subFlowInstanceTreeResult = buildFlowInstanceTree(flowInstanceMappingPO.getSubFlowInstanceId(), interruptCondition);
        FlowInstancePOJO subFlowInstance = subFlowInstanceTreeResult.getRootFlowInstancePOJO();
        subFlowInstance.setBelongNodeInstance(nodeInstance);
        nodeInstance.getSubFlowInstanceList().add(subFlowInstance);
        if (subFlowInstanceTreeResult.needInterrupt()) {
          flowInstanceTreeResult.setInterruptNodeInstancePOJO(subFlowInstanceTreeResult.getInterruptNodeInstancePOJO());
          return flowInstanceTreeResult;
        }
      }
    }
    return flowInstanceTreeResult;
  }

  public long count(Predicate predicate) {
    return flowInstanceRepository.count(predicate);
  }

  public List<FlowInstance> find(Predicate predicate, Integer page, Integer size) {
    return flowInstanceRepository.find(predicate, page, size);
  }

  public FlowInstance findById(String flowInstanceId) {
    return flowInstanceRepository.selectByFlowInstanceId(flowInstanceId);
  }

  private static class FlowInstancePOJO {
    private String id;
    private NodeInstancePOJO belongNodeInstance;
    private List<NodeInstancePOJO> nodeInstanceList = new ArrayList<>();

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public NodeInstancePOJO getBelongNodeInstance() {
      return belongNodeInstance;
    }

    public void setBelongNodeInstance(NodeInstancePOJO belongNodeInstance) {
      this.belongNodeInstance = belongNodeInstance;
    }

    public List<NodeInstancePOJO> getNodeInstanceList() {
      return nodeInstanceList;
    }

    public void setNodeInstanceList(List<NodeInstancePOJO> nodeInstanceList) {
      this.nodeInstanceList = nodeInstanceList;
    }
  }

  private static class NodeInstancePOJO {

    private String id;
    private FlowInstancePOJO flowInstance;
    private List<FlowInstancePOJO> subFlowInstanceList = new ArrayList<>();

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public FlowInstancePOJO getFlowInstance() {
      return flowInstance;
    }

    public void setFlowInstance(FlowInstancePOJO flowInstance) {
      this.flowInstance = flowInstance;
    }

    public List<FlowInstancePOJO> getSubFlowInstanceList() {
      return subFlowInstanceList;
    }

    public void setSubFlowInstanceList(List<FlowInstancePOJO> subFlowInstanceList) {
      this.subFlowInstanceList = subFlowInstanceList;
    }
  }

  private static class FlowInstanceTreeResult {
    private FlowInstancePOJO rootFlowInstancePOJO;
    private NodeInstancePOJO interruptNodeInstancePOJO;

    public FlowInstancePOJO getRootFlowInstancePOJO() {
      return rootFlowInstancePOJO;
    }

    public void setRootFlowInstancePOJO(FlowInstancePOJO rootFlowInstancePOJO) {
      this.rootFlowInstancePOJO = rootFlowInstancePOJO;
    }

    public NodeInstancePOJO getInterruptNodeInstancePOJO() {
      return interruptNodeInstancePOJO;
    }

    public void setInterruptNodeInstancePOJO(NodeInstancePOJO interruptNodeInstancePOJO) {
      this.interruptNodeInstancePOJO = interruptNodeInstancePOJO;
    }

    public boolean needInterrupt() {
      return interruptNodeInstancePOJO != null;
    }
  }

  /**
   * When build a flowInstanceAndNodeInstance tree,
   * we allow timely interruption to improve response.
   */
  private interface InterruptCondition {

    /**
     * Returns true when the condition is match
     *
     * @param nodeInstancePO
     * @return
     */
    boolean match(NodeInstance nodeInstancePO);
  }
}
