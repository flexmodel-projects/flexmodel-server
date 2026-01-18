package dev.flexmodel.domain.model.flow.repository;

import dev.flexmodel.codegen.entity.InstanceData;

/**
 * @author cjbi
 */
public interface InstanceDataRepository {
  InstanceData select(String projectId, String flowInstanceId, String instanceDataId);

  InstanceData selectRecentOne(String projectId, String flowInstanceId);

  int insert(InstanceData instanceData);

  int updateData(String projectId, InstanceData instanceData);

  int insertOrUpdate(String projectId, InstanceData mergeEntity);
}
