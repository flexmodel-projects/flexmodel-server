package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.InstanceData;

/**
 * @author cjbi
 */
public interface InstanceDataRepository {
  InstanceData select(String projectId, String flowInstanceId, String instanceDataId);

  InstanceData selectRecentOne(String projectId, String flowInstanceId);

  int insert(String projectId, InstanceData instanceData);

  int updateData(String projectId, InstanceData instanceData);

  int insertOrUpdate(String projectId, InstanceData mergeEntity);
}
