package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.FlowInstance;

/**
 * @author cjbi
 */
public interface FlowInstanceRepository {
  FlowInstance selectByFlowInstanceId(String flowInstanceId);

  int insert(FlowInstance flowInstance);

  void updateStatus(String flowInstanceId, int status);

  void updateStatus(FlowInstance flowInstance, int status);
}
