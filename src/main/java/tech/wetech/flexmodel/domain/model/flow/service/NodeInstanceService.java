package tech.wetech.flexmodel.domain.model.flow.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.codegen.entity.FlowInstance;
import tech.wetech.flexmodel.codegen.entity.NodeInstance;
import tech.wetech.flexmodel.domain.model.flow.common.FlowElementType;
import tech.wetech.flexmodel.domain.model.flow.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.NodeInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.util.FlowModelUtil;

import java.util.List;
import java.util.Map;

@Singleton
public class NodeInstanceService {

  @Inject
  private NodeInstanceRepository nodeInstanceRepository;

  @Inject
  private FlowInstanceRepository processInstanceRepository;

  @Inject
  private FlowDeploymentRepository flowDeploymentRepository;

  @Inject
  private FlowInstanceService flowInstanceService;

  public NodeInstance selectByNodeInstanceId(String flowInstanceId, String nodeInstanceId, boolean effectiveForSubFlowInstance) {
    NodeInstance nodeInstance = nodeInstanceRepository.selectByNodeInstanceId(flowInstanceId, nodeInstanceId);
    if (!effectiveForSubFlowInstance) {
      return nodeInstance;
    }
    if (nodeInstance != null) {
      return nodeInstance;
    }
    String subFlowInstanceId = flowInstanceService.getFlowInstanceIdByRootFlowInstanceIdAndNodeInstanceId(flowInstanceId, nodeInstanceId);
    return nodeInstanceRepository.selectByNodeInstanceId(subFlowInstanceId, nodeInstanceId);
  }

  public NodeInstance selectRecentEndNode(String flowInstanceId) {
    FlowInstance rootFlowInstance = processInstanceRepository.selectByFlowInstanceId(flowInstanceId);
    FlowDeployment rootFlowDeployment = flowDeploymentRepository.selectByDeployId(rootFlowInstance.getFlowDeployId());
    Map<String, FlowElement> rootFlowElementMap = FlowModelUtil.getFlowElementMap(rootFlowDeployment.getFlowModel());

    List<NodeInstance> nodeInstanceList = nodeInstanceRepository.selectDescByFlowInstanceId(flowInstanceId);
    for (NodeInstance nodeInstance : nodeInstanceList) {
      int elementType = FlowModelUtil.getElementType(nodeInstance.getNodeKey(), rootFlowElementMap);
      if (elementType == FlowElementType.END_EVENT) {
        return nodeInstance;
      }
    }
    return null;
  }
}
