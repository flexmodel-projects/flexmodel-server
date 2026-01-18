package dev.flexmodel.infrastructure.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.Calendar;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.*;
import dev.flexmodel.codegen.entity.*;
import dev.flexmodel.shared.utils.JsonUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * @author cjbi
 */
@Slf4j
public class FmJobStore implements JobStore {
  FmJobRepository jobRepository;

  private String instanceId;
  private String instanceName = "flexmodel-scheduler";
  private int threadPoolSize = 10;
  private long acquireRetryDelay = 1000L;
  private ClassLoadHelper loadHelper;
  private SchedulerSignaler signaler;
  private static final String DEFAULT_SCHEMA_NAME = "system"; // kept for backward compat but unused

  // 进程内非并发作业占用表（仅用于 @DisallowConcurrentExecution）
  private final ConcurrentHashMap<JobKey, AtomicInteger> nonConcurrentRunning = new ConcurrentHashMap<>();

  // JobDataMap 的内存级最新值缓存（避免 DB 提交时序导致的覆盖）
  private final ConcurrentHashMap<JobKey, JobDataMap> lastJobData = new ConcurrentHashMap<>();

  @Override
  public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) throws SchedulerConfigException {
    this.loadHelper = loadHelper;
    this.signaler = signaler;
    this.jobRepository = new FmJobRepository();
    log.info("FmJobStore initialized with FmJobRepository");
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
    try {
      QrtzJobDetail existingJob = jobRepository.findJobDetail(instanceName, newJob.getKey().getName(), newJob.getKey().getGroup());

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

      jobRepository.upsertJobDetail(jobDetail);

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
    try {
      List<QrtzTrigger> triggers = jobRepository.findTriggersByJob(instanceName, jobKey.getName(), jobKey.getGroup());
      for (QrtzTrigger t : triggers) {
        jobRepository.deleteTrigger(instanceName, t.getTriggerName(), t.getTriggerGroup());
      }
      jobRepository.deleteJob(instanceName, jobKey.getName(), jobKey.getGroup());
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
    try {
      QrtzJobDetail jobDetail = jobRepository.findJobDetail(instanceName, jobKey.getName(), jobKey.getGroup());

      if (jobDetail == null) {
        return null;
      }

      JobDetail jd = deserializeJobDetail(jobDetail);
      // 叠加内存缓存的最新 JobDataMap（若存在）
      JobDataMap cached = lastJobData.get(jd.getKey());
      if (cached != null) {
        jd.getJobDataMap().putAll(cached);
      }
      return jd;
    } catch (Exception e) {
      log.error("Failed to retrieve job: {}", jobKey, e);
      throw new JobPersistenceException("Failed to retrieve job", e);
    }
  }

  @Override
  public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws ObjectAlreadyExistsException, JobPersistenceException {
    try {
      QrtzTrigger existingTrigger = jobRepository.findTrigger(instanceName, newTrigger.getKey().getName(), newTrigger.getKey().getGroup());

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

      jobRepository.upsertTrigger(trigger);

      // 如果是简单触发器，存储额外信息
      if (newTrigger instanceof SimpleTrigger) {
        QrtzSimpleTrigger simple = new QrtzSimpleTrigger();
        simple.setSchedName(instanceName);
        simple.setTriggerName(newTrigger.getKey().getName());
        simple.setTriggerGroup(newTrigger.getKey().getGroup());
        simple.setRepeatCount((long) ((SimpleTrigger) newTrigger).getRepeatCount());
        simple.setRepeatInterval(((SimpleTrigger) newTrigger).getRepeatInterval());
        simple.setTimesTriggered((long) ((SimpleTrigger) newTrigger).getTimesTriggered());
        jobRepository.upsertSimpleTrigger(simple);
      } else if (newTrigger instanceof CronTrigger) {
        QrtzCronTrigger cron = new QrtzCronTrigger();
        cron.setSchedName(instanceName);
        cron.setTriggerName(newTrigger.getKey().getName());
        cron.setTriggerGroup(newTrigger.getKey().getGroup());
        cron.setCronExpression(((CronTrigger) newTrigger).getCronExpression());
        cron.setTimeZoneId(((CronTrigger) newTrigger).getTimeZone() != null ? ((CronTrigger) newTrigger).getTimeZone().getID() : null);
        jobRepository.upsertCronTrigger(cron);
      }

      log.debug("Stored trigger: {}", newTrigger.getKey());
    } catch (Exception e) {
      log.error("Failed to store trigger: {}", newTrigger.getKey(), e);
      throw new JobPersistenceException("Failed to store trigger", e);
    }
  }

  @Override
  public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    try {
      jobRepository.deleteTrigger(instanceName, triggerKey.getName(), triggerKey.getGroup());
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
    try {
      QrtzTrigger trigger = jobRepository.findTrigger(instanceName, triggerKey.getName(), triggerKey.getGroup());

      if (trigger == null) {
        return null;
      }

      return (OperableTrigger) deserializeTrigger(trigger);
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
    try {
      jobRepository.clearAll(instanceName);
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to clear all scheduling data", e);
    }
  }

  @Override
  public void storeCalendar(String name, org.quartz.Calendar calendar, boolean replaceExisting, boolean updateTriggers) throws ObjectAlreadyExistsException, JobPersistenceException {
    try {
      QrtzCalendar existing = jobRepository.findCalendar(instanceName, name);
      if (existing != null && !replaceExisting) {
        throw new ObjectAlreadyExistsException("Calendar already exists: " + name);
      }
      QrtzCalendar c = new QrtzCalendar();
      c.setSchedName(instanceName);
      c.setCalendarName(name);
      // 使用Quartz的Calendar序列化为JSON
      c.setCalendar(serializeQrtzCalendar(calendar));
      jobRepository.upsertCalendar(c);
      if (updateTriggers) {
        jobRepository.updateTriggersStateByCalendarName(instanceName, name, Trigger.TriggerState.NORMAL.name());
      }
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to store calendar", e);
    }
  }

  @Override
  public boolean removeCalendar(String calName) throws JobPersistenceException {
    try {
      jobRepository.deleteCalendar(instanceName, calName);
      return true;
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to remove calendar", e);
    }
  }

  @Override
  public org.quartz.Calendar retrieveCalendar(String calName) throws JobPersistenceException {
    try {
      QrtzCalendar c = jobRepository.findCalendar(instanceName, calName);
      if (c == null) return null;
      return deserializeQrtzCalendar(c.getCalendar());
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to retrieve calendar", e);
    }
  }

  @Override
  public int getNumberOfJobs() throws JobPersistenceException {
    try {
      return Math.toIntExact(jobRepository.countJobs(instanceName));
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to count jobs", e);
    }
  }

  @Override
  public int getNumberOfTriggers() throws JobPersistenceException {
    try {
      return Math.toIntExact(jobRepository.countTriggers(instanceName));
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to count triggers", e);
    }
  }

  @Override
  public int getNumberOfCalendars() throws JobPersistenceException {
    try {
      return Math.toIntExact(jobRepository.countCalendars(instanceName));
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to count calendars", e);
    }
  }

  @Override
  public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    try {
      List<QrtzJobDetail> jobDetails = jobRepository.findJobs(instanceName);

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
    try {
      List<QrtzTrigger> triggers = jobRepository.findTriggers(instanceName);

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
    try {
      List<QrtzJobDetail> list = jobRepository.findJobs(instanceName);
      return list.stream().map(QrtzJobDetail::getJobGroup).distinct().collect(Collectors.toList());
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to get job group names", e);
    }
  }

  @Override
  public List<String> getTriggerGroupNames() throws JobPersistenceException {
    try {
      List<QrtzTrigger> list = jobRepository.findTriggers(instanceName);
      return list.stream().map(QrtzTrigger::getTriggerGroup).distinct().collect(Collectors.toList());
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to get trigger group names", e);
    }
  }

  @Override
  public List<String> getCalendarNames() throws JobPersistenceException {
    try {
      List<QrtzCalendar> list = jobRepository.findCalendars(instanceName);
      return list.stream().map(QrtzCalendar::getCalendarName).distinct().collect(Collectors.toList());
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to get calendar names", e);
    }
  }

  @Override
  public List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException {
    try {
      List<QrtzTrigger> triggers = jobRepository.findTriggersByJob(instanceName, jobKey.getName(), jobKey.getGroup());
      List<OperableTrigger> list = new ArrayList<>();
      for (QrtzTrigger t : triggers) {
        try {
          list.add(deserializeTrigger(t));
        } catch (Exception ignored) {
        }
      }
      return list;
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to get triggers for job", e);
    }
  }

  @Override
  public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
    try {
      QrtzTrigger t = jobRepository.findTrigger(instanceName, triggerKey.getName(), triggerKey.getGroup());
      if (t == null) return Trigger.TriggerState.NONE;
      try {
        return Trigger.TriggerState.valueOf(t.getTriggerState());
      } catch (Exception e) {
        return Trigger.TriggerState.NONE;
      }
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to get trigger state", e);
    }
  }

  @Override
  public void resetTriggerFromErrorState(TriggerKey triggerKey) throws JobPersistenceException {
    jobRepository.updateTriggerState(instanceName, triggerKey.getName(), triggerKey.getGroup(), Trigger.TriggerState.NORMAL.name());
  }

  @Override
  public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    jobRepository.updateTriggerState(instanceName, triggerKey.getName(), triggerKey.getGroup(), Trigger.TriggerState.PAUSED.name());
  }

  @Override
  public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    try {
      List<QrtzTrigger> list = jobRepository.findTriggers(instanceName);
      Set<String> groups = new HashSet<>();
      for (QrtzTrigger t : list) {
        TriggerKey key = new TriggerKey(t.getTriggerName(), t.getTriggerGroup());
        if (matcher.isMatch(key)) {
          jobRepository.updateTriggerState(instanceName, key.getName(), key.getGroup(), Trigger.TriggerState.PAUSED.name());
          groups.add(t.getTriggerGroup());
        }
      }
      return groups;
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to pause triggers", e);
    }
  }

  @Override
  public void pauseJob(JobKey jobKey) throws JobPersistenceException {
    try {
      List<QrtzTrigger> triggers = jobRepository.findTriggersByJob(instanceName, jobKey.getName(), jobKey.getGroup());
      for (QrtzTrigger t : triggers) {
        jobRepository.updateTriggerState(instanceName, t.getTriggerName(), t.getTriggerGroup(), Trigger.TriggerState.PAUSED.name());
      }
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to pause job", e);
    }
  }

  @Override
  public Collection<String> pauseJobs(GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
    try {
      List<QrtzJobDetail> jobs = jobRepository.findJobs(instanceName);
      Set<String> groups = new HashSet<>();
      for (QrtzJobDetail j : jobs) {
        JobKey key = new JobKey(j.getJobName(), j.getJobGroup());
        if (groupMatcher.isMatch(key)) {
          pauseJob(key);
          groups.add(j.getJobGroup());
        }
      }
      return groups;
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to pause jobs", e);
    }
  }

  @Override
  public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    jobRepository.updateTriggerState(instanceName, triggerKey.getName(), triggerKey.getGroup(), Trigger.TriggerState.NORMAL.name());
  }

  @Override
  public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    try {
      List<QrtzTrigger> list = jobRepository.findTriggers(instanceName);
      Set<String> groups = new HashSet<>();
      for (QrtzTrigger t : list) {
        TriggerKey key = new TriggerKey(t.getTriggerName(), t.getTriggerGroup());
        if (matcher.isMatch(key)) {
          jobRepository.updateTriggerState(instanceName, key.getName(), key.getGroup(), Trigger.TriggerState.NORMAL.name());
          groups.add(t.getTriggerGroup());
        }
      }
      return groups;
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to resume triggers", e);
    }
  }

  @Override
  public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
    try {
      List<QrtzTrigger> list = jobRepository.findTriggers(instanceName);
      list.removeIf(t -> !Trigger.TriggerState.PAUSED.name().equals(t.getTriggerState()));
      return list.stream().map(QrtzTrigger::getTriggerGroup).collect(Collectors.toSet());
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to get paused trigger groups", e);
    }
  }

  @Override
  public void resumeJob(JobKey jobKey) throws JobPersistenceException {
    try {
      List<QrtzTrigger> triggers = jobRepository.findTriggersByJob(instanceName, jobKey.getName(), jobKey.getGroup());
      for (QrtzTrigger t : triggers) {
        jobRepository.updateTriggerState(instanceName, t.getTriggerName(), t.getTriggerGroup(), Trigger.TriggerState.NORMAL.name());
      }
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to resume job", e);
    }
  }

  @Override
  public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    try {
      List<QrtzJobDetail> jobs = jobRepository.findJobs(instanceName);
      Set<String> groups = new HashSet<>();
      for (QrtzJobDetail j : jobs) {
        JobKey key = new JobKey(j.getJobName(), j.getJobGroup());
        if (matcher.isMatch(key)) {
          resumeJob(key);
          groups.add(j.getJobGroup());
        }
      }
      return groups;
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to resume jobs", e);
    }
  }

  @Override
  public void pauseAll() throws JobPersistenceException {
    try {
      jobRepository.updateAllTriggersState(instanceName, Trigger.TriggerState.PAUSED.name());
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to pause all triggers", e);
    }
  }

  @Override
  public void resumeAll() throws JobPersistenceException {
    try {
      jobRepository.updateAllTriggersState(instanceName, Trigger.TriggerState.NORMAL.name());
    } catch (Exception e) {
      throw new JobPersistenceException("Failed to resume all triggers", e);
    }
  }

  @Override
  public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow) throws JobPersistenceException {
    try {
      List<QrtzTrigger> triggers = jobRepository.findDueTriggers(instanceName, noLaterThan, timeWindow, maxCount);

      List<OperableTrigger> result = new ArrayList<>();
      Set<JobKey> acquiredJobKeys = new HashSet<>();
      for (QrtzTrigger trigger : triggers) {
        try {
          OperableTrigger operableTrigger = deserializeTrigger(trigger);
          if (operableTrigger != null) {
            JobDetail jd = retrieveJob(operableTrigger.getJobKey());
            boolean nonConcurrent = isJobNonConcurrent(operableTrigger.getJobKey(), jd);
            if (nonConcurrent && jd != null && acquiredJobKeys.contains(jd.getKey())) {
              // 将未入选本批的同作业触发器直接置为 BLOCKED，等待当前执行结束
              jobRepository.updateTriggerState(instanceName, trigger.getTriggerName(), trigger.getTriggerGroup(), Trigger.TriggerState.BLOCKED.name());
              continue; // 避免同一作业并发
            }
            // 更新触发器状态为 NORMAL（此实现不引入 ACQUIRED 状态，但已筛掉并发）
            jobRepository.updateTriggerState(instanceName, trigger.getTriggerName(), trigger.getTriggerGroup(),
              Trigger.TriggerState.NORMAL.name());
            result.add(operableTrigger);
            if (nonConcurrent && jd != null) {
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
    try {
      jobRepository.updateTriggerState(instanceName, trigger.getKey().getName(), trigger.getKey().getGroup(), Trigger.TriggerState.NORMAL.name());
    } catch (Exception e) {
      throw new RuntimeException(new JobPersistenceException("Failed to release acquired trigger", e));
    }
  }

  @Override
  public List<TriggerFiredResult> triggersFired(List<OperableTrigger> triggers) throws JobPersistenceException {
    List<TriggerFiredResult> results = new ArrayList<>();
    try {
      for (OperableTrigger t : triggers) {
        JobDetail jobDetail = retrieveJob(t.getJobKey());
        boolean acquiredNonConcurrentLock = false;
        JobKey acquiredJobKey = null;

        // 基于 @DisallowConcurrentExecution 的进程内并发控制：未获取锁则跳过本次触发
        boolean nonConcurrent = isJobNonConcurrent(t.getJobKey(), jobDetail);
        if (nonConcurrent) {
          AtomicInteger counter = nonConcurrentRunning.computeIfAbsent(jobDetail.getKey(), k -> new AtomicInteger(0));
          int valueAfterInc = counter.incrementAndGet();
          if (valueAfterInc > 1) {
            // 未能获取“独占”执行权，回退并跳过
            counter.decrementAndGet();
            // 保持触发器为 NORMAL，不更新 prev/next，跳过本次
            continue;
          }
          acquiredNonConcurrentLock = true;
          acquiredJobKey = jobDetail.getKey();
          // 将同一作业的其他 NORMAL 触发器置为 BLOCKED，避免并发调度
          jobRepository.blockOtherTriggersOfJob(instanceName, jobDetail.getKey(), t.getKey());
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
          jobRepository.updateTriggerFireTimes(instanceName, t.getKey().getName(), t.getKey().getGroup(), prev, next);

          // 如果没有下一次触发，标记完成
          if (t.getNextFireTime() == null) {
            jobRepository.updateTriggerState(instanceName, t.getKey().getName(), t.getKey().getGroup(), Trigger.TriggerState.COMPLETE.name());
          } else {
            jobRepository.updateTriggerState(instanceName, t.getKey().getName(), t.getKey().getGroup(), Trigger.TriggerState.NORMAL.name());
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
    String state = switch (triggerInstCode) {
      case DELETE_TRIGGER -> Trigger.TriggerState.NONE.name();
      case SET_TRIGGER_COMPLETE -> Trigger.TriggerState.COMPLETE.name();
      case SET_TRIGGER_ERROR -> Trigger.TriggerState.ERROR.name();
      case SET_ALL_JOB_TRIGGERS_COMPLETE -> Trigger.TriggerState.COMPLETE.name();
      case SET_ALL_JOB_TRIGGERS_ERROR -> Trigger.TriggerState.ERROR.name();
      default -> Trigger.TriggerState.NORMAL.name();
    };
    jobRepository.updateTriggerState(instanceName, trigger.getKey().getName(), trigger.getKey().getGroup(), state);

    // 持久化 JobDataMap（支持 @PersistJobDataAfterExecution）：优先依据 JobDetail 标志，回退到 DB 标志
    if (jobDetail != null && (jobDetail.isPersistJobDataAfterExecution()
                              || shouldPersistJobData(jobDetail.getKey()))) {
      // 先更新内存缓存，保证下一次 retrieveJob 能见到最新值
      lastJobData.put(jobDetail.getKey(), new JobDataMap(new HashMap<>(jobDetail.getJobDataMap())));
      jobRepository.updateJobData(instanceName, jobDetail.getKey(), jobDetail.getJobDataMap());
    }

    // 释放 @DisallowConcurrentExecution 的进程内“锁”
    if (isJobNonConcurrent(jobDetail != null ? jobDetail.getKey() : null, jobDetail)) {
      // 解锁数据库中同作业被置为 BLOCKED 的触发器
      jobRepository.unblockBlockedTriggersOfJob(instanceName, jobDetail.getKey());
      try {
        if (signaler != null) {
          signaler.signalSchedulingChange(0L);
        }
      } catch (Exception ignored) {
      }
      AtomicInteger counter = nonConcurrentRunning.get(jobDetail.getKey());
      if (counter != null) {
        int remaining = counter.decrementAndGet();
        if (remaining <= 0) {
          nonConcurrentRunning.remove(jobDetail.getKey(), counter);
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
  private OperableTrigger deserializeTrigger(QrtzTrigger trigger) throws Exception {
    JobDetail jobDetail = retrieveJob(new JobKey(trigger.getJobName(), trigger.getJobGroup()));
    if (jobDetail == null) {
      return null;
    }

    OperableTrigger operableTrigger;
    if ("CRON".equals(trigger.getTriggerType())) {
      operableTrigger = deserializeCronTrigger(trigger, jobDetail);
    } else if ("SIMPLE".equals(trigger.getTriggerType())) {
      operableTrigger = deserializeSimpleTrigger(trigger, jobDetail);
    } else {
      throw new JobPersistenceException("Unknown trigger type: " + trigger.getTriggerType());
    }

    return operableTrigger;
  }

  /**
   * 反序列化Cron触发器
   */
  private OperableTrigger deserializeCronTrigger(QrtzTrigger trigger, JobDetail jobDetail) throws Exception {
    QrtzCronTrigger cronMeta = jobRepository.findCronMeta(trigger.getSchedName(), trigger.getTriggerName(), trigger.getTriggerGroup());

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
  private OperableTrigger deserializeSimpleTrigger(QrtzTrigger trigger, JobDetail jobDetail) throws Exception {
    QrtzSimpleTrigger simpleMeta = jobRepository.findSimpleMeta(trigger.getSchedName(), trigger.getTriggerName(), trigger.getTriggerGroup());

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
   * 根据 DB 或 JobDetail 判定作业是否为非并发。
   */
  private boolean isJobNonConcurrent(JobKey jobKey, JobDetail jobDetail) {
    if (jobDetail != null && jobDetail.isConcurrentExecutionDisallowed()) {
      return true;
    }
    if (jobKey == null) return false;
    try {
      QrtzJobDetail jd = jobRepository.findJobDetail(instanceName, jobKey.getName(), jobKey.getGroup());
      return jd != null && Boolean.TRUE.equals(jd.getIsNonconcurrent());
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 判定是否应持久化 JobDataMap（依据 DB 中 is_update_data）。
   */
  private boolean shouldPersistJobData(JobKey jobKey) {
    if (jobKey == null) return false;
    try {
      QrtzJobDetail jd = jobRepository.findJobDetail(instanceName, jobKey.getName(), jobKey.getGroup());
      return jd != null && Boolean.TRUE.equals(jd.getIsUpdateData());
    } catch (Exception e) {
      return false;
    }
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
