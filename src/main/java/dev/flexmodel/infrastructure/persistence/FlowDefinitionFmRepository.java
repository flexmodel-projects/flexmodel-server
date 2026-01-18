package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.FlowDefinition;
import dev.flexmodel.domain.model.flow.repository.FlowDefinitionRepository;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

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
      .where(field(FlowDefinition::getProjectId).eq(flowDefinition.getProjectId()).and(field(FlowDefinition::getFlowModuleId).eq(flowDefinition.getFlowModuleId())))
      .execute();
  }

  @Override
  public FlowDefinition selectByModuleId(String projectId, String flowModuleId) {
    return session.dsl()
      .selectFrom(FlowDefinition.class)
      .where(field(FlowDefinition::getProjectId).eq(projectId).and(field(FlowDefinition::getFlowModuleId).eq(flowModuleId)))
      .executeOne();
  }

  @Override
  public List<FlowDefinition> find(String projectId, Predicate filter, Integer page, Integer size) {
    return session.dsl().selectFrom(FlowDefinition.class)
      .page(page, size)
      .where(field(FlowDefinition::getProjectId).eq(projectId).and(filter))
      .orderByDesc("id")
      .execute();
  }

  @Override
  public long count(String projectId, Predicate filter) {
    return session.dsl().selectFrom(FlowDefinition.class)
      .where(field(FlowDefinition::getProjectId).eq(projectId).and(filter))
      .count();
  }
}


