package tech.wetech.flexmodel.domain.model.flow.repository;

import tech.wetech.flexmodel.codegen.entity.FlowInstance;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface FlowInstanceRepository {
  FlowInstance selectByFlowInstanceId(String flowInstanceId);

  int insert(FlowInstance flowInstance);

  void updateStatus(String flowInstanceId, int status);

  void updateStatus(FlowInstance flowInstance, int status);

  long count(Predicate predicate);

  List<FlowInstance> find(Predicate predicate, Integer page, Integer size);

}
