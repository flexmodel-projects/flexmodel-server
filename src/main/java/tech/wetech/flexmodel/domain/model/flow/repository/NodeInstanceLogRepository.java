package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.NodeInstanceLog;

import java.util.List;

/**
 * @author cjbi
 */
public interface NodeInstanceLogRepository {
  int insert(NodeInstanceLog nodeInstanceLog);

  boolean insertList(List<NodeInstanceLog> nodeInstanceLogList);
}
