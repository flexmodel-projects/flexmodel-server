package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.FlowDefinition;

/**
 * @author cjbi
 */
public interface FlowDefinitionRepository {
  int insert(FlowDefinition flowDefinition);

  int updateByModuleId(FlowDefinition flowDefinition);

  FlowDefinition selectByModuleId(String flowModuleId);
}
