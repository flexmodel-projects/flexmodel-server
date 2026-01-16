package tech.wetech.flexmodel.domain.model.flow.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.FlowDefinition;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDefinitionRepository;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class FlowDefinitionService {

  @Inject
  FlowDefinitionRepository flowDefinitionRepository;

  public List<FlowDefinition> find(String projectId, Predicate filter, Integer page, Integer size) {
    return flowDefinitionRepository.find(projectId, filter, page, size);
  }

  public long count(String projectId, Predicate filter) {
    return flowDefinitionRepository.count(projectId, filter);
  }

}
