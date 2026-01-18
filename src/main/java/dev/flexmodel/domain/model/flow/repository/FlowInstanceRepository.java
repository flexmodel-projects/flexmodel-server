package dev.flexmodel.domain.model.flow.repository;

import dev.flexmodel.codegen.entity.FlowInstance;
import dev.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface FlowInstanceRepository {
  FlowInstance selectByFlowInstanceId(String projectId, String flowInstanceId);

  int insert(FlowInstance flowInstance);

  void updateStatus(String projectId, String flowInstanceId, int status);

  void updateStatus(String projectId, FlowInstance flowInstance, int status);

  long count(String projectId, Predicate predicate);

  List<FlowInstance> find(String projectId, Predicate predicate, Integer page, Integer size);

}
