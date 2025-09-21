package tech.wetech.flexmodel.domain.model.schedule;

import tech.wetech.flexmodel.codegen.entity.Trigger;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface TriggerRepository {
  Trigger findById(String id);

  Trigger save(Trigger trigger);

  void deleteById(String id);

  List<Trigger> find(Predicate filter, Integer page, Integer size);

  long count(Predicate filter);
}
