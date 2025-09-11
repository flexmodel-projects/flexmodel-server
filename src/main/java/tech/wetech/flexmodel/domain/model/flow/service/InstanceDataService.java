package tech.wetech.flexmodel.domain.model.flow.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.codegen.entity.*;
import tech.wetech.flexmodel.domain.model.flow.dto.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.repository.*;
import tech.wetech.flexmodel.domain.model.flow.shared.common.FlowElementType;
import tech.wetech.flexmodel.domain.model.flow.shared.util.FlowModelUtil;

import java.util.Map;

@Singleton
public class InstanceDataService {

  @Inject
  private InstanceDataRepository instanceDataRepository;

  @Inject
  private FlowInstanceRepository flowInstanceRepository;

  @Inject
  private FlowDeploymentRepository flowDeploymentRepository;

  @Inject
  private NodeInstanceRepository nodeInstanceRepository;

  @Inject
  private FlowInstanceMappingRepository flowInstanceMappingRepository;

  @Inject
  private FlowInstanceService flowInstanceService;

  public InstanceData select(String flowInstanceId, String instanceDataId, boolean effectiveForSubFlowInstance) {
    InstanceData instanceData = instanceDataRepository.select(flowInstanceId, instanceDataId);
    if (!effectiveForSubFlowInstance) {
      return instanceData;
    }
    if (instanceData != null) {
      return instanceData;
    }
    String subFlowInstanceId = flowInstanceService.getFlowInstanceIdByRootFlowInstanceIdAndInstanceDataId(flowInstanceId, instanceDataId);
    return instanceDataRepository.select(subFlowInstanceId, instanceDataId);
  }

  public InstanceData select(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    InstanceData instanceData = instanceDataRepository.selectRecentOne(flowInstanceId);
    if (!effectiveForSubFlowInstance) {
      return instanceData;
    }
    FlowInstance flowInstance = flowInstanceRepository.selectByFlowInstanceId(flowInstanceId);
    FlowDeployment flowDeployment = flowDeploymentRepository.selectByDeployId(flowInstance.getFlowDeployId());
    Map<String, FlowElement> flowElementMap = FlowModelUtil.getFlowElementMap(flowDeployment.getFlowModel());

    NodeInstance nodeInstance = nodeInstanceRepository.selectRecentOne(flowInstanceId);
    int elementType = FlowModelUtil.getElementType(nodeInstance.getNodeKey(), flowElementMap);
    if (elementType != FlowElementType.CALL_ACTIVITY) {
      return instanceDataRepository.selectRecentOne(flowInstanceId);
    } else {
      FlowInstanceMapping flowInstanceMapping = flowInstanceMappingRepository.selectFlowInstanceMapping(flowInstanceId, nodeInstance.getNodeInstanceId());
      return select(flowInstanceMapping.getSubFlowInstanceId(), true);
    }
  }
}
