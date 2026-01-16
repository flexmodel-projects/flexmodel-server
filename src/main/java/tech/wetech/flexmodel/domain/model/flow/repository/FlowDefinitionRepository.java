package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.FlowDefinition;
import tech.wetech.flexmodel.query.Predicate;

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
