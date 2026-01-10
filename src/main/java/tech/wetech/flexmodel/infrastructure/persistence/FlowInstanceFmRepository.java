package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.FlowInstance;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowInstanceRepository;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

@ApplicationScoped
public class FlowInstanceFmRepository implements FlowInstanceRepository {

  @Inject
  Session session;

  @Override
  public FlowInstance selectByFlowInstanceId(String projectId, String flowInstanceId) {
    return session.dsl()
      .selectFrom(FlowInstance.class)
      .where(field(FlowInstance::getProjectId).eq(projectId).and(field(FlowInstance::getFlowInstanceId).eq(flowInstanceId)))
      .executeOne();
  }

  @Override
  public int insert(FlowInstance flowInstance) {
    return session.dsl().insertInto(FlowInstance.class).values(flowInstance).execute();
  }

  @Override
  public void updateStatus(String projectId, String flowInstanceId, int status) {
    session.dsl()
      .update(FlowInstance.class)
      .set(FlowInstance::getStatus, status)
      .where(field(FlowInstance::getProjectId).eq(projectId).and(field(FlowInstance::getFlowInstanceId).eq(flowInstanceId)))
      .execute();
  }

  @Override
  public void updateStatus(String projectId, FlowInstance flowInstance, int status) {
    session.dsl()
      .update(FlowInstance.class)
      .set(FlowInstance::getStatus, status)
      .where(field(FlowInstance::getProjectId).eq(projectId).and(field(FlowInstance::getFlowInstanceId).eq(flowInstance.getFlowInstanceId())))
      .execute();
  }

  @Override
  public long count(String projectId, Predicate predicate) {
    return session.dsl()
      .selectFrom(FlowInstance.class)
      .where(field(FlowInstance::getProjectId).eq(projectId).and(predicate))
      .count();
  }

  @Override
  public List<FlowInstance> find(String projectId, Predicate predicate, Integer page, Integer size) {
    return session.dsl()
      .selectFrom(FlowInstance.class)
      .where(field(FlowInstance::getProjectId).eq(projectId).and(predicate))
      .page(page, size)
      .orderByDesc(FlowInstance::getCreateTime)
      .execute();
  }

}


