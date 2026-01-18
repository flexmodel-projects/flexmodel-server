package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Trigger;
import dev.flexmodel.domain.model.schedule.TriggerRepository;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class TriggerFmRepository implements TriggerRepository {

  @Inject
  Session session;

  @Override
  public Trigger findById(String projectId, String id) {
    return session.dsl()
      .select()
      .from(Trigger.class)
      .where(field(Trigger::getProjectId).eq(projectId).and(field(Trigger::getId).eq(id)))
      .executeOne();
  }

  @Override
  public Trigger save(String projectId, Trigger trigger) {
    session.dsl()
      .mergeInto(Trigger.class)
      .values(trigger)
      .execute();
    return trigger;
  }

  @Override
  public void deleteById(String projectId, String id) {
    session.dsl()
      .deleteFrom(Trigger.class)
      .where(field(Trigger::getProjectId).eq(projectId).and(field(Trigger::getId).eq(id)))
      .execute();
  }

  @Override
  public List<Trigger> find(String projectId, Predicate filter, Integer page, Integer size) {
    return session.dsl()
      .select()
      .from(Trigger.class)
      .where(field(Trigger::getProjectId).eq(projectId).and(filter))
      .page(page, size)
      .orderByDesc(Trigger::getCreatedAt)
      .execute();
  }

  @Override
  public long count(String projectId, Predicate filter) {
    return session.dsl()
      .select()
      .from(Trigger.class)
      .where(field(Trigger::getProjectId).eq(projectId).and(filter))
      .count();
  }

}
