package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.NodeInstance;

import java.util.List;

/**
 * @author cjbi
 */
public interface NodeInstanceRepository {
  int insert(NodeInstance nodeInstance);

  boolean insertOrUpdateList(List<NodeInstance> nodeInstanceList);

  NodeInstance selectByNodeInstanceId(String flowInstanceId, String nodeInstanceId);

  NodeInstance selectBySourceInstanceId(String flowInstanceId, String sourceNodeInstanceId, String nodeKey);

  NodeInstance selectRecentOne(String flowInstanceId);

  NodeInstance selectRecentActiveOne(String flowInstanceId);

  NodeInstance selectRecentCompletedOne(String flowInstanceId);

  NodeInstance selectEnabledOne(String flowInstanceId);

  List<NodeInstance> selectByFlowInstanceId(String flowInstanceId);

  List<NodeInstance> selectDescByFlowInstanceId(String flowInstanceId);

  void updateStatus(NodeInstance nodeInstance, int status);

  List<NodeInstance> selectByFlowInstanceIdAndNodeKey(String flowInstanceId, String nodeKey);
}
