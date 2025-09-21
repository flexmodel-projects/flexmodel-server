package tech.wetech.flexmodel.domain.model.schedule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Trigger;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class TriggerService {

  @Inject
  TriggerRepository triggerRepository;

  public Trigger findById(String id) {
    return triggerRepository.findById(id);
  }

  public Trigger save(Trigger trigger) {
    return triggerRepository.save(trigger);
  }

  public void deleteById(String id) {
    triggerRepository.deleteById(id);
  }

  public long count(Predicate filter) {
    return triggerRepository.count(filter);
  }

  public List<Trigger> find(Predicate filter, Integer page, Integer size) {
    return triggerRepository.find(filter, page, size);
  }

}
