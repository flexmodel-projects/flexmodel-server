package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.InstanceData;
import tech.wetech.flexmodel.domain.model.flow.repository.InstanceDataRepository;
import tech.wetech.flexmodel.session.Session;

import static tech.wetech.flexmodel.query.Expressions.field;

@ApplicationScoped
public class InstanceDataFmRepository implements InstanceDataRepository {

  @Inject
  Session session;

  @Override
  public InstanceData select(String projectId, String flowInstanceId, String instanceDataId) {
    return session.dsl().selectFrom(InstanceData.class)
      .where(field(InstanceData::getProjectId).eq(projectId).and(field(InstanceData::getFlowInstanceId).eq(flowInstanceId)
        .and(field(InstanceData::getInstanceDataId).eq(instanceDataId))))
      .executeOne();
  }

  @Override
  public InstanceData selectRecentOne(String projectId, String flowInstanceId) {
    return session.dsl().selectFrom(InstanceData.class)
      .where(field(InstanceData::getProjectId).eq(projectId).and(field(InstanceData::getFlowInstanceId).eq(flowInstanceId)))
      .orderByDesc(InstanceData::getId)
      .limit(1)
      .executeOne();
  }

  @Override
  public int insert(String projectId, InstanceData instanceData) {
    return session.dsl().insertInto(InstanceData.class).values(instanceData).execute();
  }

  @Override
  public int updateData(String projectId, InstanceData instanceData) {
    return session.dsl().update(InstanceData.class)
      .values(instanceData)
      .where(field(InstanceData::getProjectId).eq(projectId).and(field(InstanceData::getId).eq(instanceData.getId())))
      .execute();
  }

  @Override
  public int insertOrUpdate(String projectId, InstanceData mergeEntity) {
    if (mergeEntity.getId() != null) {
      return updateData(projectId, mergeEntity);
    }
    return insert(projectId, mergeEntity);
  }
}


