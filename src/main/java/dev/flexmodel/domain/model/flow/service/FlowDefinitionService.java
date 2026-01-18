package dev.flexmodel.domain.model.flow.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.FlowDefinition;
import dev.flexmodel.domain.model.flow.repository.FlowDefinitionRepository;
import dev.flexmodel.query.Predicate;

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
