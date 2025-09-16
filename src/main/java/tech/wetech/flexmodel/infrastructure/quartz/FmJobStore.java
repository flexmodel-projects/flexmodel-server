package tech.wetech.flexmodel.infrastructure.quartz;

import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.Calendar;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.*;
import tech.wetech.flexmodel.codegen.entity.*;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@Slf4j
public class FmJobStore implements JobStore {

  SessionFactory sessionFactory;

  private String instanceId;
  private String instanceName = "flexmodel-scheduler";
  private int threadPoolSize = 10;
  private long acquireRetryDelay = 1000L;
  private ClassLoadHelper loadHelper;
  private SchedulerSignaler signaler;
  private static final String DEFAULT_SCHEMA_NAME = "system";

  // 进程内非并发作业占用表（仅用于 @DisallowConcurrentExecution）
  private final ConcurrentHashMap<JobKey, AtomicInteger> nonConcurrentRunning = new ConcurrentHashMap<>();

  @Override
  public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) throws SchedulerConfigException {
    this.loadHelper = loadHelper;
    this.signaler = signaler;
    this.sessionFactory = CDI.current().select(SessionFactory.class).get();
    log.info("FmJobStore initialized with Flexmodel ORM");
  }

  @Override
  public void schedulerStarted() throws SchedulerException {

  }

  @Override
  public void schedulerPaused() {

  }

  @Override
  public void schedulerResumed() {

  }

  @Override
  public void shutdown() {

  }

  @Override
  public boolean supportsPersistence() {
    return true;
  }

  @Override
  public long getEstimatedTimeToReleaseAndAcquireTrigger() {
    return 100L;
  }

  @Override
  public boolean isClustered() {
    return false;
  }

  @Override
  public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger) throws ObjectAlreadyExistsException, JobPersistenceException {
    storeJob(newJob, false);
    storeTrigger(newTrigger, false);
  }

  @Override
  public void storeJob(JobDetail newJob, boolean replaceExisting) throws ObjectAlreadyExistsException, JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      // 检查任务是否已存在
      QrtzJobDetail existingJob = session.dsl()
        .selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(instanceName)
          .and(field(QrtzJobDetail::getJobName).eq(newJob.getKey().getName()))
          .and(field(QrtzJobDetail::getJobGroup).eq(newJob.getKey().getGroup())))
        .executeOne();

      if (existingJob != null && !replaceExisting) {
        throw new ObjectAlreadyExistsException(newJob);
      }

      // 创建或更新任务详情
      QrtzJobDetail jobDetail = new QrtzJobDetail();
      jobDetail.setSchedName(instanceName);
      jobDetail.setJobName(newJob.getKey().getName());
      jobDetail.setJobGroup(newJob.getKey().getGroup());
      jobDetail.setDescription(newJob.getDescription());
      jobDetail.setJobClassName(newJob.getJobClass().getName());
      jobDetail.setIsDurable(newJob.isDurable());
      jobDetail.setIsNonconcurrent(newJob.isConcurrentExecutionDisallowed());
      jobDetail.setIsUpdateData(newJob.isPersistJobDataAfterExecution());
      jobDetail.setRequestsRecovery(newJob.requestsRecovery());
      jobDetail.setJobData(newJob.getJobDataMap());

      session.dsl()
        .mergeInto(QrtzJobDetail.class)
        .values(jobDetail)
        .execute();

      log.debug("Stored job: {}", newJob.getKey());
    } catch (Exception e) {
      log.error("Failed to store job: {}", newJob.getKey(), e);
      throw new JobPersistenceException("Failed to store job", e);
    }
  }

  @Override
  public void storeJobsAndTriggers(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws ObjectAlreadyExistsException, JobPersistenceException {
    for (Map.Entry<JobDetail, Set<? extends Trigger>> e : triggersAndJobs.entrySet()) {
      storeJob(e.getKey(), replace);
      for (Trigger t : e.getValue()) {
        storeTrigger((OperableTrigger) t, replace);
      }
    }
  }

  @Override
  public boolean removeJob(JobKey jobKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      // 删除触发器
      List<QrtzTrigger> triggers = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getJobName).eq(jobKey.getName()))
          .and(field(QrtzTrigger::getJobGroup).eq(jobKey.getGroup())))
        .execute();

      for (QrtzTrigger t : triggers) {
        session.dsl()
          .deleteFrom(QrtzTrigger.class)
          .where(field(QrtzTrigger::getSchedName).eq(instanceName)
            .and(field(QrtzTrigger::getTriggerName).eq(t.getTriggerName()))
            .and(field(QrtzTrigger::getTriggerGroup).eq(t.getTriggerGroup())))
          .execute();

        session.dsl()
          .deleteFrom(QrtzSimpleTrigger.class)
          .where(field(QrtzSimpleTrigger::getSchedName).eq(instanceName)
            .and(field(QrtzSimpleTrigger::getTriggerName).eq(t.getTriggerName()))
            .and(field(QrtzSimpleTrigger::getTriggerGroup).eq(t.getTriggerGroup())))
          .execute();

        session.dsl()
          .deleteFrom(QrtzCronTrigger.class)
          .where(field(QrtzCronTrigger::getSchedName).eq(instanceName)
            .and(field(QrtzCronTrigger::getTriggerName).eq(t.getTriggerName()))
            .and(field(QrtzCronTrigger::getTriggerGroup).eq(t.getTriggerGroup())))
          .execute();
      }

      // 删除任务
      session.dsl()
        .deleteFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(instanceName)
          .and(field(QrtzJobDetail::getJobName).eq(jobKey.getName()))
          .and(field(QrtzJobDetail::getJobGroup).eq(jobKey.getGroup())))
        .execute();
      return true;
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to remove job", e);
    }
  }

  @Override
  public boolean removeJobs(List<JobKey> jobKeys) throws JobPersistenceException {
    boolean ok = true;
    for (JobKey k : jobKeys) {
      ok &= removeJob(k);
    }
    return ok;
  }

  @Override
  public JobDetail retrieveJob(JobKey jobKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      QrtzJobDetail jobDetail = session.dsl()
        .selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(instanceName)
          .and(field(QrtzJobDetail::getJobName).eq(jobKey.getName()))
          .and(field(QrtzJobDetail::getJobGroup).eq(jobKey.getGroup())))
        .executeOne();

      if (jobDetail == null) {
        return null;
      }

      return deserializeJobDetail(jobDetail);
    } catch (Exception e) {
      log.error("Failed to retrieve job: {}", jobKey, e);
      throw new JobPersistenceException("Failed to retrieve job", e);
    }
  }

  @Override
  public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws ObjectAlreadyExistsException, JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      // 检查触发器是否已存在
      QrtzTrigger existingTrigger = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getTriggerName).eq(newTrigger.getKey().getName()))
          .and(field(QrtzTrigger::getTriggerGroup).eq(newTrigger.getKey().getGroup())))
        .executeOne();

      if (existingTrigger != null && !replaceExisting) {
        throw new ObjectAlreadyExistsException(newTrigger);
      }

      // 计算首次触发时间（若未计算），需考虑 Calendar
      if (newTrigger.getNextFireTime() == null) {
        org.quartz.Calendar cal = null;
        if (newTrigger.getCalendarName() != null) {
          cal = retrieveCalendar(newTrigger.getCalendarName());
        }
        newTrigger.computeFirstFireTime(cal);
      }

      // 创建或更新触发器
      QrtzTrigger trigger = new QrtzTrigger();
      trigger.setSchedName(instanceName);
      trigger.setTriggerName(newTrigger.getKey().getName());
      trigger.setTriggerGroup(newTrigger.getKey().getGroup());
      trigger.setJobName(newTrigger.getJobKey().getName());
      trigger.setJobGroup(newTrigger.getJobKey().getGroup());
      trigger.setDescription(newTrigger.getDescription());
      trigger.setNextFireTime(newTrigger.getNextFireTime() != null ? newTrigger.getNextFireTime().getTime() : null);
      trigger.setPrevFireTime(newTrigger.getPreviousFireTime() != null ? newTrigger.getPreviousFireTime().getTime() : null);
      trigger.setPriority(newTrigger.getPriority());
      trigger.setTriggerState(Trigger.TriggerState.NORMAL.name());
      trigger.setTriggerType(getTriggerType(newTrigger));
      trigger.setStartTime(newTrigger.getStartTime().getTime());
      trigger.setEndTime(newTrigger.getEndTime() != null ? newTrigger.getEndTime().getTime() : null);
      trigger.setCalendarName(newTrigger.getCalendarName());
      trigger.setMisfireInstr(newTrigger.getMisfireInstruction());
      trigger.setJobData(newTrigger.getJobDataMap());

      session.dsl()
        .mergeInto(QrtzTrigger.class)
        .values(trigger)
        .execute();

      // 如果是简单触发器，存储额外信息
      if (newTrigger instanceof SimpleTrigger) {
        storeSimpleTrigger((SimpleTrigger) newTrigger, session);
      } else if (newTrigger instanceof CronTrigger) {
        storeCronTrigger((CronTrigger) newTrigger, session);
      }

      log.debug("Stored trigger: {}", newTrigger.getKey());
    } catch (Exception e) {
      log.error("Failed to store trigger: {}", newTrigger.getKey(), e);
      throw new JobPersistenceException("Failed to store trigger", e);
    }
  }

  @Override
  public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .deleteFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getTriggerName).eq(triggerKey.getName()))
          .and(field(QrtzTrigger::getTriggerGroup).eq(triggerKey.getGroup())))
        .execute();

      session.dsl()
        .deleteFrom(QrtzSimpleTrigger.class)
        .where(field(QrtzSimpleTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzSimpleTrigger::getTriggerName).eq(triggerKey.getName()))
          .and(field(QrtzSimpleTrigger::getTriggerGroup).eq(triggerKey.getGroup())))
        .execute();

      session.dsl()
        .deleteFrom(QrtzCronTrigger.class)
        .where(field(QrtzCronTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzCronTrigger::getTriggerName).eq(triggerKey.getName()))
          .and(field(QrtzCronTrigger::getTriggerGroup).eq(triggerKey.getGroup())))
        .execute();
      return true;
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to remove trigger", e);
    }
  }

  @Override
  public boolean removeTriggers(List<TriggerKey> triggerKeys) throws JobPersistenceException {
    boolean ok = true;
    for (TriggerKey k : triggerKeys) {
      ok &= removeTrigger(k);
    }
    return ok;
  }

  @Override
  public boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) throws JobPersistenceException {
    removeTrigger(triggerKey);
    storeTrigger(newTrigger, false);
    return true;
  }

  @Override
  public OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      QrtzTrigger trigger = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getTriggerName).eq(triggerKey.getName()))
          .and(field(QrtzTrigger::getTriggerGroup).eq(triggerKey.getGroup())))
        .executeOne();

      if (trigger == null) {
        return null;
      }

      return (OperableTrigger) deserializeTrigger(trigger, session);
    } catch (Exception e) {
      log.error("Failed to retrieve trigger: {}", triggerKey, e);
      throw new JobPersistenceException("Failed to retrieve trigger", e);
    }
  }

  @Override
  public boolean checkExists(JobKey jobKey) throws JobPersistenceException {
    return retrieveJob(jobKey) != null;
  }

  @Override
  public boolean checkExists(TriggerKey triggerKey) throws JobPersistenceException {
    return retrieveTrigger(triggerKey) != null;
  }

  @Override
  public void clearAllSchedulingData() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl().deleteFrom(QrtzSimpleTrigger.class).execute();
      session.dsl().deleteFrom(QrtzCronTrigger.class).execute();
      session.dsl().deleteFrom(QrtzTrigger.class).execute();
      session.dsl().deleteFrom(QrtzJobDetail.class).execute();
      session.dsl().deleteFrom(QrtzCalendar.class).execute();
    }
  }

  @Override
  public void storeCalendar(String name, org.quartz.Calendar calendar, boolean replaceExisting, boolean updateTriggers) throws ObjectAlreadyExistsException, JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      QrtzCalendar existing = session.dsl()
        .selectFrom(QrtzCalendar.class)
        .where(field(QrtzCalendar::getSchedName).eq(instanceName)
          .and(field(QrtzCalendar::getCalendarName).eq(name)))
        .executeOne();
      if (existing != null && !replaceExisting) {
        throw new ObjectAlreadyExistsException("Calendar already exists: " + name);
      }
      QrtzCalendar c = new QrtzCalendar();
      c.setSchedName(instanceName);
      c.setCalendarName(name);
      // 使用Quartz的Calendar序列化为JSON
      c.setCalendar(serializeQrtzCalendar(calendar));
      session.dsl().mergeInto(QrtzCalendar.class).values(c).execute();
      if (updateTriggers) {
        // 受该calendar影响的触发器需要重新计算next_fire_time，这里只标记为WAITING
        session.dsl()
          .update(QrtzTrigger.class)
          .set(QrtzTrigger::getTriggerState, Trigger.TriggerState.NORMAL.name())
          .where(field(QrtzTrigger::getSchedName).eq(instanceName)
            .and(field(QrtzTrigger::getCalendarName).eq(name)))
          .execute();
      }
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to store calendar", e);
    }
  }

  @Override
  public boolean removeCalendar(String calName) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .deleteFrom(QrtzCalendar.class)
        .where(field(QrtzCalendar::getSchedName).eq(instanceName)
          .and(field(QrtzCalendar::getCalendarName).eq(calName)))
        .execute();
      return true;
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to remove calendar", e);
    }
  }

  @Override
  public org.quartz.Calendar retrieveCalendar(String calName) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      QrtzCalendar c = session.dsl()
        .selectFrom(QrtzCalendar.class)
        .where(field(QrtzCalendar::getSchedName).eq(instanceName)
          .and(field(QrtzCalendar::getCalendarName).eq(calName)))
        .executeOne();
      if (c == null) return null;
      return deserializeQrtzCalendar(c.getCalendar());
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to retrieve calendar", e);
    }
  }

  @Override
  public int getNumberOfJobs() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return Math.toIntExact(session.dsl().selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(instanceName))
        .count());
    }
  }

  @Override
  public int getNumberOfTriggers() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return Math.toIntExact(session.dsl().selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName))
        .count());
    }
  }

  @Override
  public int getNumberOfCalendars() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      return Math.toIntExact(session.dsl().selectFrom(QrtzCalendar.class)
        .where(field(QrtzCalendar::getSchedName).eq(instanceName))
        .count());
    }
  }

  @Override
  public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzJobDetail> jobDetails = session.dsl()
        .selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(instanceName))
        .execute();

      return jobDetails.stream()
        .map(job -> new JobKey(job.getJobName(), job.getJobGroup()))
        .filter(matcher::isMatch)
        .collect(Collectors.toSet());
    } catch (Exception e) {
      log.error("Failed to get job keys", e);
      throw new JobPersistenceException("Failed to get job keys", e);
    }
  }

  @Override
  public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzTrigger> triggers = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName))
        .execute();

      return triggers.stream()
        .map(trigger -> new TriggerKey(trigger.getTriggerName(), trigger.getTriggerGroup()))
        .filter(matcher::isMatch)
        .collect(Collectors.toSet());
    } catch (Exception e) {
      log.error("Failed to get trigger keys", e);
      throw new JobPersistenceException("Failed to get trigger keys", e);
    }
  }

  @Override
  public List<String> getJobGroupNames() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzJobDetail> list = session.dsl()
        .selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(instanceName))
        .execute();
      return list.stream().map(QrtzJobDetail::getJobGroup).distinct().collect(Collectors.toList());
    }
  }

  @Override
  public List<String> getTriggerGroupNames() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzTrigger> list = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName))
        .execute();
      return list.stream().map(QrtzTrigger::getTriggerGroup).distinct().collect(Collectors.toList());
    }
  }

  @Override
  public List<String> getCalendarNames() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzCalendar> list = session.dsl()
        .selectFrom(QrtzCalendar.class)
        .where(field(QrtzCalendar::getSchedName).eq(instanceName))
        .execute();
      return list.stream().map(QrtzCalendar::getCalendarName).distinct().collect(Collectors.toList());
    }
  }

  @Override
  public List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzTrigger> triggers = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getJobName).eq(jobKey.getName()))
          .and(field(QrtzTrigger::getJobGroup).eq(jobKey.getGroup())))
        .execute();
      List<OperableTrigger> list = new ArrayList<>();
      for (QrtzTrigger t : triggers) {
        try {
          list.add(deserializeTrigger(t, session));
        } catch (Exception ignored) {
        }
      }
      return list;
    }
  }

  @Override
  public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      QrtzTrigger t = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getTriggerName).eq(triggerKey.getName()))
          .and(field(QrtzTrigger::getTriggerGroup).eq(triggerKey.getGroup())))
        .executeOne();
      if (t == null) return Trigger.TriggerState.NONE;
      try {
        return Trigger.TriggerState.valueOf(t.getTriggerState());
      } catch (Exception e) {
        return Trigger.TriggerState.NONE;
      }
    }
  }

  @Override
  public void resetTriggerFromErrorState(TriggerKey triggerKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      updateTriggerState(triggerKey.getName(), triggerKey.getGroup(), Trigger.TriggerState.NORMAL.name(), session);
    }
  }

  @Override
  public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      updateTriggerState(triggerKey.getName(), triggerKey.getGroup(), Trigger.TriggerState.PAUSED.name(), session);
    }
  }

  @Override
  public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzTrigger> list = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName))
        .execute();
      Set<String> groups = new HashSet<>();
      for (QrtzTrigger t : list) {
        TriggerKey key = new TriggerKey(t.getTriggerName(), t.getTriggerGroup());
        if (matcher.isMatch(key)) {
          updateTriggerState(key.getName(), key.getGroup(), Trigger.TriggerState.PAUSED.name(), session);
          groups.add(t.getTriggerGroup());
        }
      }
      return groups;
    }
  }

  @Override
  public void pauseJob(JobKey jobKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzTrigger> triggers = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getJobName).eq(jobKey.getName()))
          .and(field(QrtzTrigger::getJobGroup).eq(jobKey.getGroup())))
        .execute();
      for (QrtzTrigger t : triggers) {
        updateTriggerState(t.getTriggerName(), t.getTriggerGroup(), Trigger.TriggerState.PAUSED.name(), session);
      }
    }
  }

  @Override
  public Collection<String> pauseJobs(GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzJobDetail> jobs = session.dsl()
        .selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(instanceName))
        .execute();
      Set<String> groups = new HashSet<>();
      for (QrtzJobDetail j : jobs) {
        JobKey key = new JobKey(j.getJobName(), j.getJobGroup());
        if (groupMatcher.isMatch(key)) {
          pauseJob(key);
          groups.add(j.getJobGroup());
        }
      }
      return groups;
    }
  }

  @Override
  public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      updateTriggerState(triggerKey.getName(), triggerKey.getGroup(), Trigger.TriggerState.NORMAL.name(), session);
    }
  }

  @Override
  public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzTrigger> list = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName))
        .execute();
      Set<String> groups = new HashSet<>();
      for (QrtzTrigger t : list) {
        TriggerKey key = new TriggerKey(t.getTriggerName(), t.getTriggerGroup());
        if (matcher.isMatch(key)) {
          updateTriggerState(key.getName(), key.getGroup(), Trigger.TriggerState.NORMAL.name(), session);
          groups.add(t.getTriggerGroup());
        }
      }
      return groups;
    }
  }

  @Override
  public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzTrigger> list = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getTriggerState).eq(Trigger.TriggerState.PAUSED.name())))
        .execute();
      return list.stream().map(QrtzTrigger::getTriggerGroup).collect(Collectors.toSet());
    }
  }

  @Override
  public void resumeJob(JobKey jobKey) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzTrigger> triggers = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getJobName).eq(jobKey.getName()))
          .and(field(QrtzTrigger::getJobGroup).eq(jobKey.getGroup())))
        .execute();
      for (QrtzTrigger t : triggers) {
        updateTriggerState(t.getTriggerName(), t.getTriggerGroup(), Trigger.TriggerState.NORMAL.name(), session);
      }
    }
  }

  @Override
  public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      List<QrtzJobDetail> jobs = session.dsl()
        .selectFrom(QrtzJobDetail.class)
        .where(field(QrtzJobDetail::getSchedName).eq(instanceName))
        .execute();
      Set<String> groups = new HashSet<>();
      for (QrtzJobDetail j : jobs) {
        JobKey key = new JobKey(j.getJobName(), j.getJobGroup());
        if (matcher.isMatch(key)) {
          resumeJob(key);
          groups.add(j.getJobGroup());
        }
      }
      return groups;
    }
  }

  @Override
  public void pauseAll() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .update(QrtzTrigger.class)
        .set(QrtzTrigger::getTriggerState, Trigger.TriggerState.PAUSED.name())
        .where(field(QrtzTrigger::getSchedName).eq(instanceName))
        .execute();
    }
  }

  @Override
  public void resumeAll() throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      session.dsl()
        .update(QrtzTrigger.class)
        .set(QrtzTrigger::getTriggerState, Trigger.TriggerState.NORMAL.name())
        .where(field(QrtzTrigger::getSchedName).eq(instanceName))
        .execute();
    }
  }

  @Override
  public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow) throws JobPersistenceException {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      // 查询需要触发的触发器
      List<QrtzTrigger> triggers = session.dsl()
        .selectFrom(QrtzTrigger.class)
        .where(field(QrtzTrigger::getSchedName).eq(instanceName)
          .and(field(QrtzTrigger::getTriggerState).eq(Trigger.TriggerState.NORMAL.name()))
          .and(field(QrtzTrigger::getNextFireTime).lte(noLaterThan + timeWindow)))
        .orderBy(QrtzTrigger::getNextFireTime)
        .orderByDesc(QrtzTrigger::getPriority)
        .page(1, maxCount)
        .execute();

      List<OperableTrigger> result = new ArrayList<>();
      Set<JobKey> acquiredJobKeys = new HashSet<>();
      for (QrtzTrigger trigger : triggers) {
        try {
          OperableTrigger operableTrigger = deserializeTrigger(trigger, session);
          if (operableTrigger != null) {
            JobDetail jd = retrieveJob(operableTrigger.getJobKey());
            if (jd != null && jd.isConcurrentExecutionDisallowed() && acquiredJobKeys.contains(jd.getKey())) {
              continue; // 避免同一作业并发
            }
            // 更新触发器状态为 NORMAL（此实现不引入 ACQUIRED 状态，但已筛掉并发）
            updateTriggerState(trigger.getTriggerName(), trigger.getTriggerGroup(),
              Trigger.TriggerState.NORMAL.name(), session);
            result.add(operableTrigger);
            if (jd != null && jd.isConcurrentExecutionDisallowed()) {
              acquiredJobKeys.add(jd.getKey());
            }
          }
        } catch (Exception e) {
          log.warn("Failed to deserialize trigger: {}", trigger.getTriggerName(), e);
        }
      }

      return result;
    } catch (Exception e) {
      log.error("Failed to acquire next triggers", e);
      throw new JobPersistenceException("Failed to acquire next triggers", e);
    }
  }

  @Override
  public void releaseAcquiredTrigger(OperableTrigger trigger) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      updateTriggerState(trigger.getKey().getName(), trigger.getKey().getGroup(), Trigger.TriggerState.NORMAL.name(), session);
    }
  }

  @Override
  public List<TriggerFiredResult> triggersFired(List<OperableTrigger> triggers) throws JobPersistenceException {
    List<TriggerFiredResult> results = new ArrayList<>();
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      for (OperableTrigger t : triggers) {
        JobDetail jobDetail = retrieveJob(t.getJobKey());
        boolean acquiredNonConcurrentLock = false;
        JobKey acquiredJobKey = null;

        // 基于 @DisallowConcurrentExecution 的进程内并发控制：未获取锁则跳过本次触发
        if (jobDetail != null && jobDetail.isConcurrentExecutionDisallowed()) {
          AtomicInteger counter = nonConcurrentRunning.computeIfAbsent(jobDetail.getKey(), k -> new AtomicInteger(0));
          int valueAfterInc = counter.incrementAndGet();
          if (valueAfterInc > 1) {
            // 未能获取“独占”执行权，回退并跳过
            counter.decrementAndGet();
            // 保持触发器为 NORMAL，不更新 prev/next，结果列表占位返回 null
            results.add(null);
            continue;
          }
          acquiredNonConcurrentLock = true;
          acquiredJobKey = jobDetail.getKey();
        }
        org.quartz.Calendar cal = null;
        if (t.getCalendarName() != null) {
          cal = retrieveCalendar(t.getCalendarName());
        }
        Date fireTime = new Date();
        try {
          // 触发前，先调用 triggered 以更新 prev/next
          t.triggered(cal);

          // 持久化 prev/nextFireTime
          Long prev = t.getPreviousFireTime() != null ? t.getPreviousFireTime().getTime() : null;
          Long next = t.getNextFireTime() != null ? t.getNextFireTime().getTime() : null;
          session.dsl()
            .update(QrtzTrigger.class)
            .set(QrtzTrigger::getPrevFireTime, prev)
            .set(QrtzTrigger::getNextFireTime, next)
            .where(field(QrtzTrigger::getSchedName).eq(instanceName)
              .and(field(QrtzTrigger::getTriggerName).eq(t.getKey().getName()))
              .and(field(QrtzTrigger::getTriggerGroup).eq(t.getKey().getGroup())))
            .execute();

          // 如果没有下一次触发，标记完成
          if (t.getNextFireTime() == null) {
            updateTriggerState(t.getKey().getName(), t.getKey().getGroup(), Trigger.TriggerState.COMPLETE.name(), session);
          } else {
            updateTriggerState(t.getKey().getName(), t.getKey().getGroup(), Trigger.TriggerState.NORMAL.name(), session);
          }

          TriggerFiredBundle b = new TriggerFiredBundle(
            jobDetail,
            t,
            cal,
            false,
            fireTime,
            t.getPreviousFireTime(),
            t.getNextFireTime(),
            null
          );
          results.add(new TriggerFiredResult(b));
        } catch (Exception ex) {
          // 构建触发结果过程中异常：如果占用了非并发锁，需要回退释放，避免死锁
          if (acquiredNonConcurrentLock && acquiredJobKey != null) {
            AtomicInteger counter = nonConcurrentRunning.get(acquiredJobKey);
            if (counter != null) {
              int remaining = counter.decrementAndGet();
              if (remaining <= 0) {
                nonConcurrentRunning.remove(acquiredJobKey, counter);
              }
            }
          }
          throw ex;
        }
      }
    } catch (Exception e) {
      throw new JobPersistenceException("Failed in triggersFired", e);
    }
    return results;
  }

  @Override
  public void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail, Trigger.CompletedExecutionInstruction triggerInstCode) {
    try (Session session = sessionFactory.createSession(DEFAULT_SCHEMA_NAME)) {
      String state = switch (triggerInstCode) {
        case DELETE_TRIGGER -> Trigger.TriggerState.NONE.name();
        case SET_TRIGGER_COMPLETE -> Trigger.TriggerState.COMPLETE.name();
        case SET_TRIGGER_ERROR -> Trigger.TriggerState.ERROR.name();
        case SET_ALL_JOB_TRIGGERS_COMPLETE -> Trigger.TriggerState.COMPLETE.name();
        case SET_ALL_JOB_TRIGGERS_ERROR -> Trigger.TriggerState.ERROR.name();
        default -> Trigger.TriggerState.NORMAL.name();
      };
      updateTriggerState(trigger.getKey().getName(), trigger.getKey().getGroup(), state, session);

      // 持久化 JobDataMap（支持 @PersistJobDataAfterExecution）：按主键更新，避免唯一约束冲突
      if (jobDetail != null && jobDetail.isPersistJobDataAfterExecution()) {
        session.dsl()
          .update(QrtzJobDetail.class)
          .set(QrtzJobDetail::getJobData, jobDetail.getJobDataMap())
          .where(field(QrtzJobDetail::getSchedName).eq(instanceName)
            .and(field(QrtzJobDetail::getJobName).eq(jobDetail.getKey().getName()))
            .and(field(QrtzJobDetail::getJobGroup).eq(jobDetail.getKey().getGroup())))
          .execute();
      }

      // 释放 @DisallowConcurrentExecution 的进程内“锁”
      if (jobDetail != null && jobDetail.isConcurrentExecutionDisallowed()) {
        AtomicInteger counter = nonConcurrentRunning.get(jobDetail.getKey());
        if (counter != null) {
          int remaining = counter.decrementAndGet();
          if (remaining <= 0) {
            nonConcurrentRunning.remove(jobDetail.getKey(), counter);
          }
        }
      }
    }
  }

  @Override
  public void setInstanceId(String schedInstId) {
    this.instanceId = schedInstId;
  }

  @Override
  public void setInstanceName(String schedName) {
    this.instanceName = schedName;
  }

  @Override
  public void setThreadPoolSize(int poolSize) {
    this.threadPoolSize = poolSize;
  }

  @Override
  public long getAcquireRetryDelay(int failureCount) {
    return acquireRetryDelay;
  }

  /**
   * 反序列化JobDataMap
   */
  private JobDataMap deserializeJobData(Object jobDataObj) {
    if (jobDataObj == null) {
      return new JobDataMap();
    }
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> dataMap = JsonUtils.getInstance().convertValue(jobDataObj, Map.class);
      return new JobDataMap(dataMap);
    } catch (Exception e) {
      log.warn("Failed to deserialize job data: {}", jobDataObj, e);
      return new JobDataMap();
    }
  }

  /**
   * 反序列化JobDetail
   */
  private JobDetail deserializeJobDetail(QrtzJobDetail jobDetail) throws Exception {
    Class<? extends Job> jobClass = loadHelper.loadClass(jobDetail.getJobClassName(), Job.class);

    JobBuilder jobBuilder = JobBuilder.newJob(jobClass)
      .withIdentity(jobDetail.getJobName(), jobDetail.getJobGroup())
      .withDescription(jobDetail.getDescription())
      .storeDurably(jobDetail.getIsDurable() != null ? jobDetail.getIsDurable() : false)
      .requestRecovery(jobDetail.getRequestsRecovery() != null ? jobDetail.getRequestsRecovery() : false);

    JobDetail job = jobBuilder.build();
    job.getJobDataMap().putAll(deserializeJobData(jobDetail.getJobData()));

    return job;
  }

  /**
   * 反序列化触发器
   */
  private OperableTrigger deserializeTrigger(QrtzTrigger trigger, Session session) throws Exception {
    JobDetail jobDetail = retrieveJob(new JobKey(trigger.getJobName(), trigger.getJobGroup()));
    if (jobDetail == null) {
      return null;
    }

    OperableTrigger operableTrigger;
    if ("CRON".equals(trigger.getTriggerType())) {
      operableTrigger = deserializeCronTrigger(trigger, jobDetail, session);
    } else if ("SIMPLE".equals(trigger.getTriggerType())) {
      operableTrigger = deserializeSimpleTrigger(trigger, jobDetail, session);
    } else {
      throw new JobPersistenceException("Unknown trigger type: " + trigger.getTriggerType());
    }

    return operableTrigger;
  }

  /**
   * 反序列化Cron触发器
   */
  private OperableTrigger deserializeCronTrigger(QrtzTrigger trigger, JobDetail jobDetail, Session session) throws Exception {
    QrtzCronTrigger cronMeta = session.dsl()
      .selectFrom(QrtzCronTrigger.class)
      .where(field(QrtzCronTrigger::getSchedName).eq(trigger.getSchedName())
        .and(field(QrtzCronTrigger::getTriggerName).eq(trigger.getTriggerName()))
        .and(field(QrtzCronTrigger::getTriggerGroup).eq(trigger.getTriggerGroup())))
      .executeOne();

    if (cronMeta == null) {
      throw new JobPersistenceException("Cron trigger not found: " + trigger.getTriggerName());
    }
    org.quartz.impl.triggers.CronTriggerImpl cron = new org.quartz.impl.triggers.CronTriggerImpl();
    cron.setName(trigger.getTriggerName());
    cron.setGroup(trigger.getTriggerGroup());
    cron.setDescription(trigger.getDescription());
    cron.setCronExpression(cronMeta.getCronExpression());
    if (cronMeta.getTimeZoneId() != null) {
      cron.setTimeZone(TimeZone.getTimeZone(cronMeta.getTimeZoneId()));
    }
    cron.setJobKey(jobDetail.getKey());
    if (trigger.getStartTime() != null) cron.setStartTime(new Date(trigger.getStartTime()));
    if (trigger.getEndTime() != null) cron.setEndTime(new Date(trigger.getEndTime()));
    if (trigger.getPriority() != null) cron.setPriority(trigger.getPriority());
    if (trigger.getMisfireInstr() != null) cron.setMisfireInstruction(trigger.getMisfireInstr());
    if (trigger.getCalendarName() != null) cron.setCalendarName(trigger.getCalendarName());
    cron.getJobDataMap().putAll(deserializeJobData(trigger.getJobData()));
    if (trigger.getPrevFireTime() != null) cron.setPreviousFireTime(new Date(trigger.getPrevFireTime()));
    if (trigger.getNextFireTime() != null) cron.setNextFireTime(new Date(trigger.getNextFireTime()));
    return cron;
  }

  /**
   * 反序列化简单触发器
   */
  private OperableTrigger deserializeSimpleTrigger(QrtzTrigger trigger, JobDetail jobDetail, Session session) throws Exception {
    QrtzSimpleTrigger simpleMeta = session.dsl()
      .selectFrom(QrtzSimpleTrigger.class)
      .where(field(QrtzSimpleTrigger::getSchedName).eq(trigger.getSchedName())
        .and(field(QrtzSimpleTrigger::getTriggerName).eq(trigger.getTriggerName()))
        .and(field(QrtzSimpleTrigger::getTriggerGroup).eq(trigger.getTriggerGroup())))
      .executeOne();

    if (simpleMeta == null) {
      throw new JobPersistenceException("Simple trigger not found: " + trigger.getTriggerName());
    }
    org.quartz.impl.triggers.SimpleTriggerImpl simple = new org.quartz.impl.triggers.SimpleTriggerImpl();
    simple.setName(trigger.getTriggerName());
    simple.setGroup(trigger.getTriggerGroup());
    simple.setDescription(trigger.getDescription());
    simple.setRepeatInterval(simpleMeta.getRepeatInterval());
    simple.setRepeatCount(simpleMeta.getRepeatCount().intValue());
    simple.setTimesTriggered(Math.toIntExact(simpleMeta.getTimesTriggered()));
    simple.setJobKey(jobDetail.getKey());
    if (trigger.getStartTime() != null) simple.setStartTime(new Date(trigger.getStartTime()));
    if (trigger.getEndTime() != null) simple.setEndTime(new Date(trigger.getEndTime()));
    if (trigger.getPriority() != null) simple.setPriority(trigger.getPriority());
    if (trigger.getMisfireInstr() != null) simple.setMisfireInstruction(trigger.getMisfireInstr());
    if (trigger.getCalendarName() != null) simple.setCalendarName(trigger.getCalendarName());
    simple.getJobDataMap().putAll(deserializeJobData(trigger.getJobData()));
    if (trigger.getPrevFireTime() != null) simple.setPreviousFireTime(new Date(trigger.getPrevFireTime()));
    if (trigger.getNextFireTime() != null) simple.setNextFireTime(new Date(trigger.getNextFireTime()));
    return simple;
  }

  /**
   * 获取触发器类型
   */
  private String getTriggerType(OperableTrigger trigger) {
    if (trigger instanceof CronTrigger) {
      return "CRON";
    } else if (trigger instanceof SimpleTrigger) {
      return "SIMPLE";
    } else {
      return "UNKNOWN";
    }
  }

  /**
   * 存储简单触发器
   */
  private void storeSimpleTrigger(SimpleTrigger trigger, Session session) {
    QrtzSimpleTrigger simpleTrigger = new QrtzSimpleTrigger();
    simpleTrigger.setSchedName(instanceName);
    simpleTrigger.setTriggerName(trigger.getKey().getName());
    simpleTrigger.setTriggerGroup(trigger.getKey().getGroup());
    simpleTrigger.setRepeatCount((long) trigger.getRepeatCount());
    simpleTrigger.setRepeatInterval(trigger.getRepeatInterval());
    simpleTrigger.setTimesTriggered((long) trigger.getTimesTriggered());

    session.dsl()
      .mergeInto(QrtzSimpleTrigger.class)
      .values(simpleTrigger)
      .execute();
  }

  /**
   * 存储Cron触发器
   */
  private void storeCronTrigger(CronTrigger trigger, Session session) {
    QrtzCronTrigger cronTrigger = new QrtzCronTrigger();
    cronTrigger.setSchedName(instanceName);
    cronTrigger.setTriggerName(trigger.getKey().getName());
    cronTrigger.setTriggerGroup(trigger.getKey().getGroup());
    cronTrigger.setCronExpression(trigger.getCronExpression());
    cronTrigger.setTimeZoneId(trigger.getTimeZone() != null ? trigger.getTimeZone().getID() : null);

    session.dsl()
      .mergeInto(QrtzCronTrigger.class)
      .values(cronTrigger)
      .execute();
  }

  /**
   * 更新触发器状态
   */
  private void updateTriggerState(String triggerName, String triggerGroup, String state, Session session) {
    session.dsl()
      .update(QrtzTrigger.class)
      .set(QrtzTrigger::getTriggerState, state)
      .where(field(QrtzTrigger::getSchedName).eq(instanceName)
        .and(field(QrtzTrigger::getTriggerName).eq(triggerName))
        .and(field(QrtzTrigger::getTriggerGroup).eq(triggerGroup)))
      .execute();
  }

  private String serializeQrtzCalendar(org.quartz.Calendar calendar) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bos);
      out.writeObject(calendar);
      out.flush();
      return Base64.getEncoder().encodeToString(bos.toByteArray());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private org.quartz.Calendar deserializeQrtzCalendar(String data) {
    try {
      byte[] bytes = Base64.getDecoder().decode(data);
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
      return (Calendar) in.readObject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
