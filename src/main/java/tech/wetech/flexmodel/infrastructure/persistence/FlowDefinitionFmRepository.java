package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.FlowDefinition;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDefinitionRepository;
import tech.wetech.flexmodel.session.Session;

import static tech.wetech.flexmodel.query.Expressions.field;

@ApplicationScoped
public class FlowDefinitionFmRepository implements FlowDefinitionRepository {

  @Inject
  Session session;

  @Override
  public int insert(FlowDefinition flowDefinition) {
    return session.dsl().insertInto(FlowDefinition.class).values(flowDefinition).execute();
  }

  @Override
  public int updateByModuleId(FlowDefinition flowDefinition) {
    return session.dsl()
      .update(FlowDefinition.class)
      .values(flowDefinition)
      .where(field(FlowDefinition::getFlowModuleId).eq(flowDefinition.getFlowModuleId()))
      .execute();
  }

  @Override
  public FlowDefinition selectByModuleId(String flowModuleId) {
    return session.dsl()
      .selectFrom(FlowDefinition.class)
      .where(field(FlowDefinition::getFlowModuleId).eq(flowModuleId))
      .executeOne();
  }
}


