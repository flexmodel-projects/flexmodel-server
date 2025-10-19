package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Trigger;
import tech.wetech.flexmodel.domain.model.schedule.TriggerRepository;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class TriggerFmRepository implements TriggerRepository {

  @Inject
  Session session;

  @Override
  public Trigger findById(String id) {
    return session.dsl()
      .select()
      .from(Trigger.class)
      .where(field(Trigger::getId).eq(id))
      .executeOne();
  }

  @Override
  public Trigger save(Trigger trigger) {
    session.dsl()
      .mergeInto(Trigger.class)
      .values(trigger)
      .execute();
    return trigger;
  }

  @Override
  public void deleteById(String id) {
    session.dsl()
      .deleteFrom(Trigger.class)
      .where(field(Trigger::getId).eq(id))
      .execute();
  }

  @Override
  public List<Trigger> find(Predicate filter, Integer page, Integer size) {
    return session.dsl()
      .select()
      .from(Trigger.class)
      .where(filter)
      .page(page, size)
      .orderByDesc(Trigger::getCreatedAt)
      .execute();
  }

  @Override
  public long count(Predicate filter) {
    return session.dsl()
      .select()
      .from(Trigger.class)
      .where(filter)
      .count();
  }

}
