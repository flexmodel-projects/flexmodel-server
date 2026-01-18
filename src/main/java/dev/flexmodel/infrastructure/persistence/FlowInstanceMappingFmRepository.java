package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.FlowInstanceMapping;
import dev.flexmodel.domain.model.flow.repository.FlowInstanceMappingRepository;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

@ApplicationScoped
public class FlowInstanceMappingFmRepository implements FlowInstanceMappingRepository {

  @Inject
  Session session;

  @Override
  public List<FlowInstanceMapping> selectFlowInstanceMappingList(String projectId, String flowInstanceId, String nodeInstanceId) {
    return session.dsl().selectFrom(FlowInstanceMapping.class)
      .where(field(FlowInstanceMapping::getProjectId).eq(projectId)
        .and(field(FlowInstanceMapping::getFlowInstanceId).eq(flowInstanceId))
        .and(field(FlowInstanceMapping::getNodeInstanceId).eq(nodeInstanceId)))
      .orderBy(FlowInstanceMapping::getCreateTime)
      .execute();
  }

  @Override
  public FlowInstanceMapping selectFlowInstanceMapping(String projectId, String flowInstanceId, String nodeInstanceId) {
    return session.dsl().selectFrom(FlowInstanceMapping.class)
      .where(field(FlowInstanceMapping::getProjectId).eq(projectId)
        .and(field(FlowInstanceMapping::getFlowInstanceId).eq(flowInstanceId))
        .and(field(FlowInstanceMapping::getNodeInstanceId).eq(nodeInstanceId)))
      .executeOne();
  }

  @Override
  public int insert(FlowInstanceMapping flowInstanceMapping) {
    return session.dsl().insertInto(FlowInstanceMapping.class).values(flowInstanceMapping).execute();
  }

  @Override
  public void updateType(String projectId, String flowInstanceId, String nodeInstanceId, int type) {
    session.dsl().update(FlowInstanceMapping.class)
      .set(FlowInstanceMapping::getType, type)
      .where(field(FlowInstanceMapping::getProjectId).eq(projectId)
        .and(field(FlowInstanceMapping::getFlowInstanceId).eq(flowInstanceId))
        .and(field(FlowInstanceMapping::getNodeInstanceId).eq(nodeInstanceId)))
      .execute();
  }
}


