package tech.wetech.flexmodel.domain.model.schedule;

import tech.wetech.flexmodel.codegen.entity.Trigger;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface TriggerRepository {
  Trigger findById(String projectId, String id);

  Trigger save(String projectId, Trigger trigger);

  void deleteById(String projectId, String id);

  List<Trigger> find(String projectId, Predicate filter, Integer page, Integer size);

  long count(String projectId, Predicate filter);
}
