package dev.flexmodel.domain.model.flow.repository;

import dev.flexmodel.codegen.entity.NodeInstance;

import java.util.List;

/**
 * @author cjbi
 */
public interface NodeInstanceRepository {

  boolean insertOrUpdateList(List<NodeInstance> nodeInstanceList);

  NodeInstance selectByNodeInstanceId(String projectId, String flowInstanceId, String nodeInstanceId);

  NodeInstance selectBySourceInstanceId(String projectId, String flowInstanceId, String sourceNodeInstanceId, String nodeKey);

  NodeInstance selectRecentOne(String projectId, String flowInstanceId);

  NodeInstance selectRecentActiveOne(String projectId, String flowInstanceId);

  NodeInstance selectRecentCompletedOne(String projectId, String flowInstanceId);

  NodeInstance selectEnabledOne(String projectId, String flowInstanceId);

  List<NodeInstance> selectByFlowInstanceId(String projectId, String flowInstanceId);

  List<NodeInstance> selectDescByFlowInstanceId(String projectId, String flowInstanceId);

  void updateStatus(String projectId, NodeInstance nodeInstance, int status);

  List<NodeInstance> selectByFlowInstanceIdAndNodeKey(String projectId, String flowInstanceId, String nodeKey);
}
