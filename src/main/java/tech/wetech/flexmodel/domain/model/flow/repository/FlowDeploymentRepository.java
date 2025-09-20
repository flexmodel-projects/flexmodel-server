package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.query.Predicate;

/**
 * @author cjbi
 */
public interface FlowDeploymentRepository {
  int insert(FlowDeployment flowDeployment);

  FlowDeployment findByDeployId(String flowDeployId);

  FlowDeployment findRecentByFlowModuleId(String flowModuleId);

  void deleteById(Long id);

  long count(Predicate filter);

}
