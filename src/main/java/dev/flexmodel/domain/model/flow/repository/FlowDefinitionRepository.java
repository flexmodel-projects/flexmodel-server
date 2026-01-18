package dev.flexmodel.domain.model.flow.repository;

import dev.flexmodel.codegen.entity.FlowDefinition;
import dev.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface FlowDefinitionRepository {
  int insert(FlowDefinition flowDefinition);

  int updateByModuleId(FlowDefinition flowDefinition);

  FlowDefinition selectByModuleId(String projectId, String flowModuleId);

  List<FlowDefinition> find(String projectId, Predicate filter, Integer page, Integer size);

  long count(String projectId, Predicate filter);

}
