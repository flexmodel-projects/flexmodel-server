package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import tech.wetech.flexmodel.session.Session;

import static tech.wetech.flexmodel.query.Expressions.field;

@ApplicationScoped
public class FlowDeploymentFmRepository implements FlowDeploymentRepository {

  @Inject
  Session session;

  @Override
  public int insert(FlowDeployment flowDeployment) {
    return session.dsl().insertInto(FlowDeployment.class).values(flowDeployment).execute();
  }

  @Override
  public FlowDeployment selectByDeployId(String flowDeployId) {
    return session.dsl()
      .selectFrom(FlowDeployment.class)
      .where(field(FlowDeployment::getFlowDeployId).eq(flowDeployId))
      .executeOne();
  }

  @Override
  public FlowDeployment selectRecentByFlowModuleId(String flowModuleId) {
    return session.dsl()
      .selectFrom(FlowDeployment.class)
      .where(field(FlowDeployment::getFlowModuleId).eq(flowModuleId))
      .orderByDesc(FlowDeployment::getId)
      .limit(1)
      .executeOne();
  }

  @Override
  public void deleteById(Long id) {
    session.dsl().deleteFrom(FlowDeployment.class)
      .where(field(FlowDeployment::getId).eq(id))
      .execute();
  }
}


