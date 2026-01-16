package tech.wetech.flexmodel.domain.model.flow.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.query.Predicate;

/**
 * @author cjbi
 */
@ApplicationScoped
public class FlowDeploymentService {

  @Inject
  FlowDeploymentRepository flowDeploymentRepository;

  public long count(String projectId, Predicate filter) {
    return flowDeploymentRepository.count(projectId, filter);
  }

  public Integer count(String projetId) {
    return (int)flowDeploymentRepository.count(projetId, Expressions.TRUE);
  }

  public FlowDeployment findRecentByFlowModuleId(String projectId, String flowKey) {
    return flowDeploymentRepository.findRecentByFlowModuleId(projectId, flowKey);
  }

  public FlowDeployment findByFlowDeployId(String projectId, String flowDeployId) {
    return flowDeploymentRepository.findByDeployId(projectId, flowDeployId);
  }
}
