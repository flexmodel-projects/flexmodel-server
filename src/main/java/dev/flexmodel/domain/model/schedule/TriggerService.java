package dev.flexmodel.domain.model.schedule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Trigger;
import dev.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class TriggerService {

  @Inject
  TriggerRepository triggerRepository;

  public Trigger findById(String projectId, String id) {
    return triggerRepository.findById(projectId, id);
  }

  public Trigger save(String projectId, Trigger trigger) {
    return triggerRepository.save(projectId, trigger);
  }

  public void deleteById(String projectId, String id) {
    triggerRepository.deleteById(projectId, id);
  }

  public long count(String projectId, Predicate filter) {
    return triggerRepository.count(projectId, filter);
  }

  public List<Trigger> find(String projectId, Predicate filter, Integer page, Integer size) {
    return triggerRepository.find(projectId, filter, page, size);
  }

}
