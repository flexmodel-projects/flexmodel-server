package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.NodeInstance;
import tech.wetech.flexmodel.domain.model.flow.repository.NodeInstanceRepository;
import tech.wetech.flexmodel.domain.model.flow.shared.common.NodeInstanceStatus;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

@ApplicationScoped
public class NodeInstanceFmRepository implements NodeInstanceRepository {

  @Inject
  Session session;

  @Override
  public int insert(NodeInstance nodeInstance) {
    return session.dsl().insertInto(NodeInstance.class).values(nodeInstance).execute();
  }

  @Override
  public boolean insertOrUpdateList(List<NodeInstance> nodeInstanceList) {
    boolean ok = true;
    for (NodeInstance ni : nodeInstanceList) {
      if (ni.getId() == null) {
        int r = session.dsl().insertInto(NodeInstance.class).values(ni).execute();
        ok &= r > 0;
      } else {
        session.dsl()
          .update(NodeInstance.class)
          .set(NodeInstance::getStatus, ni.getStatus())
          .set(NodeInstance::getModifyTime, ni.getModifyTime())
          .where(field(NodeInstance::getId).eq(ni.getId()))
          .execute();
      }
    }
    return ok;
  }

  @Override
  public NodeInstance selectByNodeInstanceId(String flowInstanceId, String nodeInstanceId) {
    return session.dsl().selectFrom(NodeInstance.class)
      .where(field(NodeInstance::getFlowInstanceId).eq(flowInstanceId)
        .and(field(NodeInstance::getNodeInstanceId).eq(nodeInstanceId)))
      .executeOne();
  }

  @Override
  public NodeInstance selectBySourceInstanceId(String flowInstanceId, String sourceNodeInstanceId, String nodeKey) {
    return session.dsl().selectFrom(NodeInstance.class)
      .where(field(NodeInstance::getFlowInstanceId).eq(flowInstanceId)
        .and(field(NodeInstance::getSourceNodeInstanceId).eq(sourceNodeInstanceId))
        .and(field(NodeInstance::getNodeKey).eq(nodeKey)))
      .executeOne();
  }

  @Override
  public NodeInstance selectRecentOne(String flowInstanceId) {
    return session.dsl().selectFrom(NodeInstance.class)
      .where(field(NodeInstance::getFlowInstanceId).eq(flowInstanceId))
      .orderByDesc(NodeInstance::getId)
      .limit(1)
      .executeOne();
  }

  @Override
  public NodeInstance selectRecentActiveOne(String flowInstanceId) {
    return session.dsl().selectFrom(NodeInstance.class)
      .where(field(NodeInstance::getFlowInstanceId).eq(flowInstanceId)
        .and(field(NodeInstance::getStatus).eq(NodeInstanceStatus.ACTIVE)))
      .orderByDesc(NodeInstance::getId)
      .limit(1)
      .executeOne();
  }

  @Override
  public NodeInstance selectRecentCompletedOne(String flowInstanceId) {
    return session.dsl().selectFrom(NodeInstance.class)
      .where(field(NodeInstance::getFlowInstanceId).eq(flowInstanceId)
        .and(field(NodeInstance::getStatus).eq(NodeInstanceStatus.COMPLETED)))
      .orderByDesc(NodeInstance::getId)
      .limit(1)
      .executeOne();
  }

  @Override
  public NodeInstance selectEnabledOne(String flowInstanceId) {
    NodeInstance active = selectRecentActiveOne(flowInstanceId);
    if (active != null) {
      return active;
    }
    return selectRecentCompletedOne(flowInstanceId);
  }

  @Override
  public List<NodeInstance> selectByFlowInstanceId(String flowInstanceId) {
    return session.dsl().selectFrom(NodeInstance.class)
      .where(field(NodeInstance::getFlowInstanceId).eq(flowInstanceId))
      .orderBy(NodeInstance::getId)
      .execute();
  }

  @Override
  public List<NodeInstance> selectDescByFlowInstanceId(String flowInstanceId) {
    return session.dsl().selectFrom(NodeInstance.class)
      .where(field(NodeInstance::getFlowInstanceId).eq(flowInstanceId))
      .orderByDesc(NodeInstance::getId)
      .execute();
  }

  @Override
  public void updateStatus(NodeInstance nodeInstance, int status) {
    session.dsl().update(NodeInstance.class)
      .set(NodeInstance::getStatus, status)
      .where(field(NodeInstance::getId).eq(nodeInstance.getId()))
      .execute();
  }

  @Override
  public List<NodeInstance> selectByFlowInstanceIdAndNodeKey(String flowInstanceId, String nodeKey) {
    return session.dsl().selectFrom(NodeInstance.class)
      .where(field(NodeInstance::getFlowInstanceId).eq(flowInstanceId)
        .and(field(NodeInstance::getNodeKey).eq(nodeKey)))
      .execute();
  }
}


