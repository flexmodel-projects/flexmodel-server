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

  FlowDefinition selectByModuleId(String flowModuleId);

  List<FlowDefinition> find(Predicate filter, Integer page, Integer size);

  long count(Predicate filter);

}
