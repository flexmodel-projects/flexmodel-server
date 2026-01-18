package dev.flexmodel.domain.model.flow.repository;

import dev.flexmodel.codegen.entity.FlowDeployment;
import dev.flexmodel.query.Predicate;

/**
 * @author cjbi
 */
public interface FlowDeploymentRepository {
  int insert(FlowDeployment flowDeployment);

  FlowDeployment findByDeployId(String projectId, String flowDeployId);

  FlowDeployment findRecentByFlowModuleId(String projectId, String flowModuleId);

  void deleteById(String projectId, Long id);

  long count(String projectId, Predicate filter);

}
