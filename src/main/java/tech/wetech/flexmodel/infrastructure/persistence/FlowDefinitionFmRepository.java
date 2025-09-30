package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.FlowDefinition;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDefinitionRepository;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

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

  @Override
  public List<FlowDefinition> find(Predicate filter, Integer page, Integer size) {
    return session.dsl().selectFrom(FlowDefinition.class)
      .page(page, size)
      .where(filter)
      .orderByDesc("id")
      .execute();
  }

  @Override
  public long count(Predicate filter) {
    return session.dsl().selectFrom(FlowDefinition.class)
      .where(filter)
      .count();
  }
}


