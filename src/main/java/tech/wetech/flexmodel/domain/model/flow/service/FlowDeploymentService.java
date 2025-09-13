package tech.wetech.flexmodel.domain.model.flow.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import tech.wetech.flexmodel.query.Predicate;

/**
 * @author cjbi
 */
@ApplicationScoped
public class FlowDeploymentService {

  @Inject
  FlowDeploymentRepository flowDeploymentRepository;

  public long count(Predicate filter) {
    return flowDeploymentRepository.count(filter);
  }

}
