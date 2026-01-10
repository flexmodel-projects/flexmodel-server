package tech.wetech.flexmodel.domain.model.flow.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.codegen.entity.FlowInstance;
import tech.wetech.flexmodel.codegen.entity.NodeInstance;
import tech.wetech.flexmodel.domain.model.flow.dto.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.NodeInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.shared.common.FlowElementType;
import tech.wetech.flexmodel.domain.model.flow.shared.util.FlowModelUtil;

import java.util.List;
import java.util.Map;

@Singleton
public class NodeInstanceService {

  @Inject
  NodeInstanceRepository nodeInstanceRepository;

  @Inject
  FlowInstanceRepository processInstanceRepository;

  @Inject
  FlowDeploymentRepository flowDeploymentRepository;

  @Inject
  FlowInstanceService flowInstanceService;

  public NodeInstance selectByNodeInstanceId(String projectId, String flowInstanceId, String nodeInstanceId, boolean effectiveForSubFlowInstance) {
    NodeInstance nodeInstance = nodeInstanceRepository.selectByNodeInstanceId(projectId, flowInstanceId, nodeInstanceId);
    if (!effectiveForSubFlowInstance) {
      return nodeInstance;
    }
    if (nodeInstance != null) {
      return nodeInstance;
    }
    String subFlowInstanceId = flowInstanceService.getFlowInstanceIdByRootFlowInstanceIdAndNodeInstanceId(projectId, flowInstanceId, nodeInstanceId);
    return nodeInstanceRepository.selectByNodeInstanceId(projectId, subFlowInstanceId, nodeInstanceId);
  }

  public NodeInstance selectRecentEndNode(String projectId, String flowInstanceId) {
    FlowInstance rootFlowInstance = processInstanceRepository.selectByFlowInstanceId(projectId, flowInstanceId);
    FlowDeployment rootFlowDeployment = flowDeploymentRepository.findByDeployId(projectId, rootFlowInstance.getFlowDeployId());
    Map<String, FlowElement> rootFlowElementMap = FlowModelUtil.getFlowElementMap(rootFlowDeployment.getFlowModel());

    List<NodeInstance> nodeInstanceList = nodeInstanceRepository.selectDescByFlowInstanceId(projectId, flowInstanceId);
    for (NodeInstance nodeInstance : nodeInstanceList) {
      int elementType = FlowModelUtil.getElementType(nodeInstance.getNodeKey(), rootFlowElementMap);
      if (elementType == FlowElementType.END_EVENT) {
        return nodeInstance;
      }
    }
    return null;
  }
}
