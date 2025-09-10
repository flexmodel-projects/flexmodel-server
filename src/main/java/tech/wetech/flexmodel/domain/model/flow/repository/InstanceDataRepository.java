package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.InstanceData;

/**
 * @author cjbi
 */
public interface InstanceDataRepository {
  InstanceData select(String flowInstanceId, String instanceDataId);

  InstanceData selectRecentOne(String flowInstanceId);

  int insert(InstanceData instanceData);

  int updateData(InstanceData instanceData);

  int insertOrUpdate(InstanceData mergeEntity);
}
