package dev.flexmodel.domain.model.flow.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.codegen.entity.FlowDeployment;
import dev.flexmodel.codegen.entity.FlowInstance;
import dev.flexmodel.codegen.entity.FlowInstanceMapping;
import dev.flexmodel.codegen.entity.NodeInstance;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import dev.flexmodel.domain.model.flow.repository.FlowInstanceMappingRepository;
import dev.flexmodel.domain.model.flow.repository.FlowInstanceRepository;
import dev.flexmodel.domain.model.flow.repository.NodeInstanceRepository;
import dev.flexmodel.domain.model.flow.shared.common.FlowElementType;
import dev.flexmodel.domain.model.flow.shared.util.FlowModelUtil;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.shared.utils.CollectionUtils;
import dev.flexmodel.shared.utils.StringUtils;

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
  public Stack<String> getNodeInstanceIdStack(String projectId, String rootFlowInstanceId, String commitNodeInstanceId) {
    if (StringUtils.isBlank(commitNodeInstanceId)) {
      LOGGER.info("getNodeInstanceId2RootStack result is empty.||rootFlowInstanceId={}||commitNodeInstanceId={}", rootFlowInstanceId, commitNodeInstanceId);
      return new Stack<>();
    }
    FlowInstanceTreeResult flowInstanceTreeResult = buildFlowInstanceTree(projectId, rootFlowInstanceId,
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
  public Set<String> getAllSubFlowInstanceIds(String projectId, String rootFlowInstanceId) {
    FlowInstanceTreeResult flowInstanceTreeResult = buildFlowInstanceTree(projectId, rootFlowInstanceId, null);
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
  public String getFlowInstanceIdByRootFlowInstanceIdAndNodeInstanceId(String projectId, String rootFlowInstanceId, String nodeInstanceId) {
    if (StringUtils.isBlank(nodeInstanceId)) {
      return "";
    }
    FlowInstanceTreeResult flowInstanceTreeResult = buildFlowInstanceTree(projectId, rootFlowInstanceId,
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
  public String getFlowInstanceIdByRootFlowInstanceIdAndInstanceDataId(String projectId, String rootFlowInstanceId, String instanceDataId) {
    if (StringUtils.isBlank(instanceDataId)) {
      return "";
    }
    FlowInstanceTreeResult flowInstanceTreeResult = buildFlowInstanceTree(projectId, rootFlowInstanceId,
      nodeInstancePO -> nodeInstancePO.getInstanceDataId().equals(instanceDataId));
    NodeInstancePOJO rightNodeInstance = flowInstanceTreeResult.getInterruptNodeInstancePOJO();
    if (rightNodeInstance == null) {
      return "";
    }
    return rightNodeInstance.getFlowInstance().getId();
  }

  // common : build a flowInstanceAndNodeInstance tree
  private FlowInstanceTreeResult buildFlowInstanceTree(String projectId, String rootFlowInstanceId, InterruptCondition interruptCondition) {
    FlowInstanceTreeResult flowInstanceTreeResult = new FlowInstanceTreeResult();
    FlowInstancePOJO flowInstance = new FlowInstancePOJO();
    flowInstance.setId(rootFlowInstanceId);
    flowInstanceTreeResult.setRootFlowInstancePOJO(flowInstance);

    FlowInstance rootFlowInstance = flowInstanceRepository.selectByFlowInstanceId(projectId, rootFlowInstanceId);
    FlowDeployment rootFlowDeployment = flowDeploymentRepository.findByDeployId(projectId, rootFlowInstance.getFlowDeployId());
    Map<String, FlowElement> rootFlowElementMap = FlowModelUtil.getFlowElementMap(rootFlowDeployment.getFlowModel());

    List<NodeInstance> nodeInstancePOList = nodeInstanceRepository.selectDescByFlowInstanceId(projectId, rootFlowInstanceId);
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
      List<FlowInstanceMapping> flowInstanceMappingPOS = flowInstanceMappingRepository.selectFlowInstanceMappingList(projectId, nodeInstancePO.getFlowInstanceId(), nodeInstancePO.getNodeInstanceId());
      for (FlowInstanceMapping flowInstanceMappingPO : flowInstanceMappingPOS) {
        FlowInstanceTreeResult subFlowInstanceTreeResult = buildFlowInstanceTree(projectId, flowInstanceMappingPO.getSubFlowInstanceId(), interruptCondition);
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

  public long count(String projectId, Predicate predicate) {
    return flowInstanceRepository.count(projectId, predicate);
  }

  public List<FlowInstance> find(String projectId, Predicate predicate, Integer page, Integer size) {
    return flowInstanceRepository.find(projectId, predicate, page, size);
  }

  public FlowInstance findById(String projectId, String flowInstanceId) {
    return flowInstanceRepository.selectByFlowInstanceId(projectId, flowInstanceId);
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
