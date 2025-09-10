package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.FlowInstanceMapping;

import java.util.List;

/**
 * @author cjbi
 */
public interface FlowInstanceMappingRepository {
  List<FlowInstanceMapping> selectFlowInstanceMappingList(String flowInstanceId, String nodeInstanceId);

  FlowInstanceMapping selectFlowInstanceMapping(String flowInstanceId, String nodeInstanceId);

  int insert(FlowInstanceMapping flowInstanceMapping);

  void updateType(String flowInstanceId, String nodeInstanceId, int type);
}
