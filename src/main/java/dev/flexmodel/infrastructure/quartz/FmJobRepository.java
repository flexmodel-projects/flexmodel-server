package dev.flexmodel.infrastructure.quartz;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import dev.flexmodel.codegen.entity.QrtzJobDetail;
import dev.flexmodel.codegen.entity.QrtzTrigger;
import dev.flexmodel.codegen.entity.QrtzCronTrigger;
import dev.flexmodel.codegen.entity.QrtzSimpleTrigger;
import dev.flexmodel.codegen.entity.QrtzSimpropTrigger;
import dev.flexmodel.codegen.entity.QrtzCalendar;
import dev.flexmodel.session.Session;
import dev.flexmodel.session.SessionFactory;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

@ApplicationScoped
public class FmJobRepository {

  private static final String DEFAULT_SCHEMA_NAME = "system";

  SessionFactory sessionFactory;

  public FmJobRepository() {
    this.sessionFactory = CDI.current().select(SessionFactory.class).get();
  }

  public QrtzJobDetail findJobDetail(String schedName, String jobName, String jobGroup) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl()
        .selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(schedName)
          .and(field(QrtzJobDetail::getJobName).eq(jobName))
          .and(field(QrtzJobDetail::getJobGroup).eq(jobGroup)))
        .executeOne();
    }
  }

  public void upsertJobDetail(QrtzJobDetail jobDetail) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl().mergeInto(QrtzJobDetail.class).values(jobDetail).execute();
    }
  }

  public void deleteJob(String schedName, String jobName, String jobGroup) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .deleteFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(schedName)
          .and(field(QrtzJobDetail::getJobName).eq(jobName))
          .and(field(QrtzJobDetail::getJobGroup).eq(jobGroup)))
        .execute();
    }
  }

  public List<QrtzTrigger> findTriggersByJob(String schedName, String jobName, String jobGroup) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(schedName)
          .and(field(QrtzTrigger::getJobName).eq(jobName))
          .and(field(QrtzTrigger::getJobGroup).eq(jobGroup)))
        .execute();
    }
  }

  public QrtzTrigger findTrigger(String schedName, String triggerName, String triggerGroup) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(schedName)
          .and(field(QrtzTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzTrigger::getTriggerGroup).eq(triggerGroup)))
        .executeOne();
    }
  }

  public void upsertTrigger(QrtzTrigger trigger) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl().mergeInto(QrtzTrigger.class).values(trigger).execute();
    }
  }

  public void upsertSimpleTrigger(QrtzSimpleTrigger simple) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl().mergeInto(QrtzSimpleTrigger.class).values(simple).execute();
    }
  }

  public void upsertCronTrigger(QrtzCronTrigger cron) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl().mergeInto(QrtzCronTrigger.class).values(cron).execute();
    }
  }

  public void upsertSimpropTrigger(QrtzSimpropTrigger simprop) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl().mergeInto(QrtzSimpropTrigger.class).values(simprop).execute();
    }
  }

  public void deleteTrigger(String schedName, String triggerName, String triggerGroup) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .deleteFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(schedName)
          .and(field(QrtzTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzTrigger::getTriggerGroup).eq(triggerGroup)))
        .execute();
      session.dsl()
        .deleteFrom(QrtzSimpleTrigger.class)
        .where(field(QrtzSimpleTrigger::getSchedName).eq(schedName)
          .and(field(QrtzSimpleTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzSimpleTrigger::getTriggerGroup).eq(triggerGroup)))
        .execute();
      session.dsl()
        .deleteFrom(QrtzCronTrigger.class)
        .where(field(QrtzCronTrigger::getSchedName).eq(schedName)
          .and(field(QrtzCronTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzCronTrigger::getTriggerGroup).eq(triggerGroup)))
        .execute();
      session.dsl()
        .deleteFrom(QrtzSimpropTrigger.class)
        .where(field(QrtzSimpropTrigger::getSchedName).eq(schedName)
          .and(field(QrtzSimpropTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzSimpropTrigger::getTriggerGroup).eq(triggerGroup)))
        .execute();
    }
  }

  public List<QrtzTrigger> findTriggers(String schedName) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl().selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(schedName))
        .execute();
    }
  }

  public List<QrtzJobDetail> findJobs(String schedName) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl().selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(schedName))
        .execute();
    }
  }

  public List<QrtzCalendar> findCalendars(String schedName) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl().selectFrom(QrtzCalendar.class)
        .where(field(QrtzCalendar::getSchedName).eq(schedName))
        .execute();
    }
  }

  public void clearAll(String schedName) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl().deleteFrom(QrtzSimpleTrigger.class).execute();
      session.dsl().deleteFrom(QrtzCronTrigger.class).execute();
      session.dsl().deleteFrom(QrtzSimpropTrigger.class).execute();
      session.dsl().deleteFrom(QrtzTrigger.class).execute();
      session.dsl().deleteFrom(QrtzJobDetail.class).execute();
      session.dsl().deleteFrom(QrtzCalendar.class).execute();
    }
  }

  public QrtzCalendar findCalendar(String schedName, String calName) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl().selectFrom(QrtzCalendar.class)
        .where(field(QrtzCalendar::getSchedName).eq(schedName)
          .and(field(QrtzCalendar::getCalendarName).eq(calName)))
        .executeOne();
    }
  }

  public void upsertCalendar(QrtzCalendar calendar) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl().mergeInto(QrtzCalendar.class).values(calendar).execute();
    }
  }

  public void deleteCalendar(String schedName, String calName) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl().
        deleteFrom(QrtzCalendar.class)
        .where(field(QrtzCalendar::getSchedName).eq(schedName)
          .and(field(QrtzCalendar::getCalendarName).eq(calName)))
        .execute();
    }
  }

  public void updateTriggersStateByCalendarName(String schedName, String calName, String state) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .update(QrtzTrigger.class)
        .set(QrtzTrigger::getTriggerState, state)
        .where(field(QrtzTrigger::getSchedName).eq(schedName)
          .and(field(QrtzTrigger::getCalendarName).eq(calName)))
        .execute();
    }
  }

  public long countJobs(String schedName) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl().selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(schedName))
        .count();
    }
  }

  public long countTriggers(String schedName) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl().selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(schedName))
        .count();
    }
  }

  public long countCalendars(String schedName) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl().selectFrom(QrtzCalendar.class)
        .where(field(QrtzCalendar::getSchedName).eq(schedName))
        .count();
    }
  }

  public List<QrtzTrigger> findDueTriggers(String schedName, long noLaterThan, long timeWindow, int maxCount) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(schedName)
          .and(field(QrtzTrigger::getTriggerState).eq(Trigger.TriggerState.NORMAL.name()))
          .and(field(QrtzTrigger::getNextFireTime).lte(noLaterThan + timeWindow)))
        .orderBy(QrtzTrigger::getNextFireTime)
        .orderByDesc(QrtzTrigger::getPriority)
        .page(1, maxCount)
        .execute();
    }
  }

  public void updateTriggerState(String schedName, String triggerName, String triggerGroup, String state) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .update(QrtzTrigger.class)
        .set(QrtzTrigger::getTriggerState, state)
        .where(field(QrtzTrigger::getSchedName).eq(schedName)
          .and(field(QrtzTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzTrigger::getTriggerGroup).eq(triggerGroup)))
        .execute();
    }
  }

  public void updateAllTriggersState(String schedName, String state) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .update(QrtzTrigger.class)
        .set(QrtzTrigger::getTriggerState, state)
        .where(field(QrtzTrigger::getSchedName).eq(schedName))
        .execute();
    }
  }

  public void blockOtherTriggersOfJob(String schedName, JobKey jobKey, TriggerKey current) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .update(QrtzTrigger.class)
        .set(QrtzTrigger::getTriggerState, Trigger.TriggerState.BLOCKED.name())
        .where(field(QrtzTrigger::getSchedName).eq(schedName)
          .and(field(QrtzTrigger::getJobName).eq(jobKey.getName()))
          .and(field(QrtzTrigger::getJobGroup).eq(jobKey.getGroup()))
          .and(field(QrtzTrigger::getTriggerState).eq(Trigger.TriggerState.NORMAL.name()))
          .and(field(QrtzTrigger::getTriggerName).ne(current.getName())
            .or(field(QrtzTrigger::getTriggerGroup).ne(current.getGroup()))))
        .execute();
    }
  }

  public void unblockBlockedTriggersOfJob(String schedName, JobKey jobKey) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .update(QrtzTrigger.class)
        .set(QrtzTrigger::getTriggerState, Trigger.TriggerState.NORMAL.name())
        .where(field(QrtzTrigger::getSchedName).eq(schedName)
          .and(field(QrtzTrigger::getJobName).eq(jobKey.getName()))
          .and(field(QrtzTrigger::getJobGroup).eq(jobKey.getGroup()))
          .and(field(QrtzTrigger::getTriggerState).eq(Trigger.TriggerState.BLOCKED.name())))
        .execute();
    }
  }

  public QrtzCronTrigger findCronMeta(String schedName, String triggerName, String triggerGroup) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl()
        .selectFrom(QrtzCronTrigger.class)
        .where(field(QrtzCronTrigger::getSchedName).eq(schedName)
          .and(field(QrtzCronTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzCronTrigger::getTriggerGroup).eq(triggerGroup)))
        .executeOne();
    }
  }

  public QrtzSimpleTrigger findSimpleMeta(String schedName, String triggerName, String triggerGroup) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl()
        .selectFrom(QrtzSimpleTrigger.class)
        .where(field(QrtzSimpleTrigger::getSchedName).eq(schedName)
          .and(field(QrtzSimpleTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzSimpleTrigger::getTriggerGroup).eq(triggerGroup)))
        .executeOne();
    }
  }

  public QrtzSimpropTrigger findSimpropMeta(String schedName, String triggerName, String triggerGroup) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return session.dsl()
        .selectFrom(QrtzSimpropTrigger.class)
        .where(field(QrtzSimpropTrigger::getSchedName).eq(schedName)
          .and(field(QrtzSimpropTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzSimpropTrigger::getTriggerGroup).eq(triggerGroup)))
        .executeOne();
    }
  }

  public void updateTriggerFireTimes(String schedName, String triggerName, String triggerGroup, Long prev, Long next) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .update(QrtzTrigger.class)
        .set(QrtzTrigger::getPrevFireTime, prev)
        .set(QrtzTrigger::getNextFireTime, next)
        .where(field(QrtzTrigger::getSchedName).eq(schedName)
          .and(field(QrtzTrigger::getTriggerName).eq(triggerName))
          .and(field(QrtzTrigger::getTriggerGroup).eq(triggerGroup)))
        .execute();
    }
  }

  public void updateJobData(String schedName, JobKey jobKey, JobDataMap jobDataMap) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .update(QrtzJobDetail.class)
        .set(QrtzJobDetail::getJobData, jobDataMap)
        .where(field(QrtzJobDetail::getSchedName).eq(schedName)
          .and(field(QrtzJobDetail::getJobName).eq(jobKey.getName()))
          .and(field(QrtzJobDetail::getJobGroup).eq(jobKey.getGroup())))
        .execute();
    }
  }
}


