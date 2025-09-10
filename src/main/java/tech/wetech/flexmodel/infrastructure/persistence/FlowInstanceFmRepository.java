package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.FlowInstance;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowInstanceRepository;
import tech.wetech.flexmodel.session.Session;

import static tech.wetech.flexmodel.query.Expressions.field;

@ApplicationScoped
public class FlowInstanceFmRepository implements FlowInstanceRepository {

  @Inject
  Session session;

  @Override
  public FlowInstance selectByFlowInstanceId(String flowInstanceId) {
    return session.dsl()
      .selectFrom(FlowInstance.class)
      .where(field(FlowInstance::getFlowInstanceId).eq(flowInstanceId))
      .executeOne();
  }

  @Override
  public int insert(FlowInstance flowInstance) {
    return session.dsl().insertInto(FlowInstance.class).values(flowInstance).execute();
  }

  @Override
  public void updateStatus(String flowInstanceId, int status) {
    session.dsl()
      .update(FlowInstance.class)
      .set(FlowInstance::getStatus, status)
      .where(field(FlowInstance::getFlowInstanceId).eq(flowInstanceId))
      .execute();
  }

  @Override
  public void updateStatus(FlowInstance flowInstance, int status) {
    session.dsl()
      .update(FlowInstance.class)
      .set(FlowInstance::getStatus, status)
      .where(field(FlowInstance::getFlowInstanceId).eq(flowInstance.getFlowInstanceId()))
      .execute();
  }
}


