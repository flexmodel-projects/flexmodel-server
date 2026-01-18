package dev.flexmodel.domain.model.flow.repository;

import dev.flexmodel.codegen.entity.FlowInstanceMapping;

import java.util.List;

/**
 * @author cjbi
 */
public interface FlowInstanceMappingRepository {
  List<FlowInstanceMapping> selectFlowInstanceMappingList(String projectId, String flowInstanceId, String nodeInstanceId);

  FlowInstanceMapping selectFlowInstanceMapping(String projectId, String flowInstanceId, String nodeInstanceId);

  int insert(FlowInstanceMapping flowInstanceMapping);

  void updateType(String projectId, String flowInstanceId, String nodeInstanceId, int type);
}
