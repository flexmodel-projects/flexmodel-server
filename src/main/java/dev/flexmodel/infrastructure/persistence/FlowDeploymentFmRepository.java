package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.FlowDeployment;
import dev.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.session.Session;

import static dev.flexmodel.query.Expressions.field;

@ApplicationScoped
public class FlowDeploymentFmRepository implements FlowDeploymentRepository {

  @Inject
  Session session;

  @Override
  public int insert(FlowDeployment flowDeployment) {
    return session.dsl().insertInto(FlowDeployment.class).values(flowDeployment).execute();
  }

  @Override
  public FlowDeployment findByDeployId(String projectId, String flowDeployId) {
    return session.dsl()
      .selectFrom(FlowDeployment.class)
      .where(field(FlowDeployment::getProjectId).eq(projectId).and(field(FlowDeployment::getFlowDeployId).eq(flowDeployId)))
      .executeOne();
  }

  @Override
  public FlowDeployment findRecentByFlowModuleId(String projectId, String flowModuleId) {
    return session.dsl()
      .selectFrom(FlowDeployment.class)
      .where(field(FlowDeployment::getProjectId).eq(projectId).and(field(FlowDeployment::getFlowModuleId).eq(flowModuleId)))
      .orderByDesc(FlowDeployment::getId)
      .limit(1)
      .executeOne();
  }

  @Override
  public void deleteById(String projectId, Long id) {
    session.dsl().deleteFrom(FlowDeployment.class)
      .where(field(FlowDeployment::getProjectId).eq(projectId).and(field(FlowDeployment::getId).eq(id)))
      .execute();
  }

  @Override
  public long count(String projectId, Predicate filter) {
    return session.dsl()
      .selectFrom(FlowDeployment.class)
      .where(field(FlowDeployment::getProjectId).eq(projectId).and(filter))
      .count();
  }

}


