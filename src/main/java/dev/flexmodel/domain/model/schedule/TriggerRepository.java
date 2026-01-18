package dev.flexmodel.domain.model.schedule;

import dev.flexmodel.codegen.entity.Trigger;
import dev.flexmodel.query.Predicate;

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
