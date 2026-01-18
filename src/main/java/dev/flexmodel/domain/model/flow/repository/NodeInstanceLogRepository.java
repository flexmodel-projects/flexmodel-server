package dev.flexmodel.domain.model.flow.repository;

import dev.flexmodel.codegen.entity.NodeInstanceLog;

import java.util.List;

/**
 * @author cjbi
 */
public interface NodeInstanceLogRepository {

  boolean insertList(List<NodeInstanceLog> nodeInstanceLogList);

}
