package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.FlowDeployment;

/**
 * @author cjbi
 */
public interface FlowDeploymentRepository {
  int insert(FlowDeployment flowDeployment);

  FlowDeployment selectByDeployId(String flowDeployId);

  FlowDeployment selectRecentByFlowModuleId(String flowModuleId);

  void deleteById(Long id);
}
