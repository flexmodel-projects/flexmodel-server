package dev.flexmodel;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quartz JobStore & Scheduler 行为覆盖测试
 * 覆盖要点：创建/删除、Simple/Cron、日历、暂停/恢复、分组操作、重新调度、作业数据、并发控制、监听器、元数据、匹配器与查询等。
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class SchedulerTest {

  @Inject
  Scheduler quartz;

  @BeforeEach
  public void clearScheduler() throws Exception {
    quartz.clear();
  }

  /**
   * 场景：验证 Scheduler 元数据获取与最基础的作业增删改查能力。
   * 断言：SchedulerMetaData 正确；作业 add/get/delete 均生效。
   */
  @Test
  public void testSchedulerMetadataAndBasicOps() throws Exception {
    assertNotNull(quartz);
    SchedulerMetaData meta = quartz.getMetaData();
    assertNotNull(meta);
    assertEquals(quartz.getSchedulerName(), meta.getSchedulerName());

    // add/get/delete
    JobDetail job = JobBuilder.newJob(NoopJob.class)
      .withIdentity("job-basic", "grp-basic")
      .storeDurably()
      .build();
    quartz.addJob(job, true);
    assertTrue(quartz.checkExists(job.getKey()));
    assertNotNull(quartz.getJobDetail(job.getKey()));
    assertTrue(quartz.deleteJob(job.getKey()));
    assertFalse(quartz.checkExists(job.getKey()));
  }

  /**
   * 场景：使用 SimpleTrigger 固定间隔执行，验证重复次数与味精触发处理。
   * 断言：计数达到 3（首次 + 两次重复），在超时时间内完成。
   */
  @Test
  public void testSimpleTriggerExecutionAndRepeat() throws Exception {
    CountDownLatch latch = new CountDownLatch(3);
    TestCounter.reset(latch);

    JobDetail job = JobBuilder.newJob(CountingJob.class)
      .withIdentity("job-simple", "grp-simple")
      .build();

    Trigger trigger = TriggerBuilder.newTrigger()
      .withIdentity("trig-simple", "grp-simple")
      .forJob(job)
      .withSchedule(SimpleScheduleBuilder.simpleSchedule()
        .withIntervalInMilliseconds(150)
        .withRepeatCount(2)
        .withMisfireHandlingInstructionFireNow())
      .startNow()
      .build();

    quartz.scheduleJob(job, trigger);
    assertTrue(latch.await(5, TimeUnit.SECONDS));
  }

  /**
   * 场景：使用 CronTrigger（每秒一次）执行作业，验证 Cron 表达式调度。
   * 断言：计数至少达到 2 次执行。
   */
  @Test
  public void testCronTriggerExecution() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    TestCounter.reset(latch);

    JobDetail job = JobBuilder.newJob(CountingJob.class)
      .withIdentity("job-cron", "grp-cron")
      .build();

    // 每秒触发，取两次即可
    Trigger trigger = TriggerBuilder.newTrigger()
      .withIdentity("trig-cron", "grp-cron")
      .forJob(job)
      .withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ?")
        .withMisfireHandlingInstructionFireAndProceed())
      .startNow()
      .build();

    quartz.scheduleJob(job, trigger);
    assertTrue(latch.await(5, TimeUnit.SECONDS));
  }

  /**
   * 场景：注册 HolidayCalendar 并排除当天，结合 Cron 调度，验证下一次触发被顺延。
   * 断言：下一次触发时间晚于当前至少 12 小时；日历可增删查。
   */
  @Test
  public void testCalendarStoreRetrieveAndUse() throws Exception {
    org.quartz.impl.calendar.HolidayCalendar cal = new org.quartz.impl.calendar.HolidayCalendar();
    // 排除当前时刻所在的日期，验证下一次触发跳到明天
    cal.addExcludedDate(new Date());
    quartz.addCalendar("holidayCal", cal, true, false);
    assertNotNull(quartz.getCalendar("holidayCal"));

    JobDetail job = JobBuilder.newJob(NoopJob.class)
      .withIdentity("job-cal", "grp-cal")
      .build();

    Trigger trigger = TriggerBuilder.newTrigger()
      .withIdentity("trig-cal", "grp-cal")
      .modifiedByCalendar("holidayCal")
      .forJob(job)
      .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")) // 每天0点
      .build();

    Date now = new Date();
    quartz.scheduleJob(job, trigger);
    Date next = quartz.getTrigger(trigger.getKey()).getNextFireTime();
    assertNotNull(next);
    // 由于今天被排除，下一次应为明日 00:00 或更晚
    java.util.Calendar tomorrowStart = java.util.Calendar.getInstance();
    tomorrowStart.setTime(now);
    tomorrowStart.set(java.util.Calendar.HOUR_OF_DAY, 0);
    tomorrowStart.set(java.util.Calendar.MINUTE, 0);
    tomorrowStart.set(java.util.Calendar.SECOND, 0);
    tomorrowStart.set(java.util.Calendar.MILLISECOND, 0);
    tomorrowStart.add(java.util.Calendar.DATE, 1);
    assertTrue(!next.before(tomorrowStart.getTime()));

    // 先取消引用该日历的触发器，再删除日历
    assertTrue(quartz.unscheduleJob(trigger.getKey()));
    assertTrue(quartz.deleteCalendar("holidayCal"));
    assertNull(quartz.getCalendar("holidayCal"));
  }

  /**
   * 场景：对单个触发器/作业以及分组进行暂停与恢复，验证状态切换。
   * 断言：触发器状态从 PAUSED 回到 NORMAL；组级 pause/resume 不报错。
   */
  @Test
  public void testPauseResumeJobTriggerAndGroups() throws Exception {
    JobDetail job = JobBuilder.newJob(NoopJob.class)
      .withIdentity("job-P", "grp-P")
      .build();
    Trigger trig = TriggerBuilder.newTrigger()
      .withIdentity("trig-P", "grp-P")
      .forJob(job)
      .withSchedule(SimpleScheduleBuilder.simpleSchedule()
        .withIntervalInSeconds(1)
        .withRepeatCount(0))
      .startNow()
      .build();
    quartz.scheduleJob(job, trig);

    quartz.pauseTrigger(trig.getKey());
    assertEquals(Trigger.TriggerState.PAUSED, quartz.getTriggerState(trig.getKey()));
    quartz.resumeTrigger(trig.getKey());
    assertEquals(Trigger.TriggerState.NORMAL, quartz.getTriggerState(trig.getKey()));

    quartz.pauseJob(job.getKey());
    quartz.resumeJob(job.getKey());

    quartz.pauseJobs(GroupMatcher.jobGroupEquals("grp-P"));
    quartz.resumeJobs(GroupMatcher.jobGroupEquals("grp-P"));

    quartz.pauseTriggers(GroupMatcher.triggerGroupEquals("grp-P"));
    quartz.resumeTriggers(GroupMatcher.triggerGroupEquals("grp-P"));
  }

  /**
   * 场景：对已存在触发器执行 reschedule，修改下一次触发时间。
   * 断言：新的 nextFireTime 晚于旧值。
   */
  @Test
  public void testRescheduleAndNextFireTimeChange() throws Exception {
    JobDetail job = JobBuilder.newJob(NoopJob.class)
      .withIdentity("job-RS", "grp-RS")
      .build();
    Trigger trig = TriggerBuilder.newTrigger()
      .withIdentity("trig-RS", "grp-RS")
      .forJob(job)
      .withSchedule(SimpleScheduleBuilder.simpleSchedule()
        .withIntervalInSeconds(10)
        .withRepeatCount(0))
      .startNow()
      .build();
    quartz.scheduleJob(job, trig);

    Date oldNext = quartz.getTrigger(trig.getKey()).getNextFireTime();
    Trigger newTrig = TriggerBuilder.newTrigger()
      .withIdentity(trig.getKey())
      .forJob(job)
      .withSchedule(SimpleScheduleBuilder.simpleSchedule()
        .withIntervalInSeconds(60)
        .withRepeatCount(0))
      .startAt(new Date(oldNext.getTime() + 30_000))
      .build();
    quartz.rescheduleJob(trig.getKey(), newTrig);
    Date newNext = quartz.getTrigger(trig.getKey()).getNextFireTime();
    assertNotNull(oldNext);
    assertNotNull(newNext);
    assertTrue(newNext.after(oldNext));
  }

  /**
   * todo 待完善
   * 场景：作业通过 @PersistJobDataAfterExecution 持久化 JobDataMap 的变更。
   * 断言：两次执行后 JobDataMap 中 count == 2。
   */
//  @Test
//  public void testJobDataMapAndPersistence() throws Exception {
//    CountDownLatch latch = new CountDownLatch(2);
//    TestCounter.reset(latch);
//
//    JobDataMap map = new JobDataMap();
//    map.put("count", 0);
//
//    JobDetail job = JobBuilder.newJob(PersistingJob.class)
//      .withIdentity("job-data", "grp-data")
//      .storeDurably()
//      .usingJobData(map)
//      .build();
//    Trigger trig = TriggerBuilder.newTrigger()
//      .withIdentity("trig-data", "grp-data")
//      .forJob(job)
//      .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//        .withIntervalInMilliseconds(100)
//        .withRepeatCount(1))
//      .startNow()
//      .build();
//
//    quartz.scheduleJob(job, trig);
//    assertTrue(latch.await(5, TimeUnit.SECONDS));
//
//    JobDetail stored = quartz.getJobDetail(job.getKey());
//    assertEquals(2, stored.getJobDataMap().getInt("count"));
//  }

  /**
   * 场景：使用 @DisallowConcurrentExecution 禁止同一作业并发执行。
   * 断言：两次几乎同时的触发不产生重叠执行（detectedOverlap 为 false）。
   */
  @Test
  public void testDisallowConcurrentExecution() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    NonConcurrentJob.reset(latch);

    JobDetail job = JobBuilder.newJob(NonConcurrentJob.class)
      .withIdentity("job-nc", "grp-nc")
      .build();

    // 两个几乎同时触发的触发器，不应并发执行
    Trigger t1 = TriggerBuilder.newTrigger()
      .withIdentity("t1", "grp-nc")
      .forJob(job)
      .startNow()
      .withSchedule(SimpleScheduleBuilder.simpleSchedule()
        .withIntervalInMilliseconds(50)
        .withRepeatCount(0))
      .build();
    Trigger t2 = TriggerBuilder.newTrigger()
      .withIdentity("t2", "grp-nc")
      .forJob(job)
      .startAt(new Date(System.currentTimeMillis() + 10))
      .withSchedule(SimpleScheduleBuilder.simpleSchedule()
        .withIntervalInMilliseconds(50)
        .withRepeatCount(0))
      .build();

    Map<JobDetail, Set<? extends Trigger>> bundle = new HashMap<>();
    bundle.put(job, new LinkedHashSet<>(Arrays.asList(t1, t2)));
    quartz.scheduleJobs(bundle, true);

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertFalse(NonConcurrentJob.detectedOverlap.get(), "不应发生并发执行");
  }

  /**
   * 场景：注册 Job/Trigger/Scheduler 监听器并触发事件回调。
   * 断言：监听器的回调计数门闩在超时内被释放。
   */
  @Test
  public void testListenersJobTriggerScheduler() throws Exception {
    TestJobListener jl = new TestJobListener();
    TestTriggerListener tl = new TestTriggerListener();
    TestSchedulerListener sl = new TestSchedulerListener();

    quartz.getListenerManager().addJobListener(jl, GroupMatcher.jobGroupContains("grp-L"));
    quartz.getListenerManager().addTriggerListener(tl, GroupMatcher.triggerGroupContains("grp-L"));
    quartz.getListenerManager().addSchedulerListener(sl);

    JobDetail job = JobBuilder.newJob(NoopJob.class)
      .withIdentity("job-L", "grp-L")
      .build();
    Trigger trig = TriggerBuilder.newTrigger()
      .withIdentity("trig-L", "grp-L")
      .forJob(job)
      .startNow()
      .build();

    quartz.scheduleJob(job, trig);

    // 等待回调发生
    assertTrue(jl.executed.await(3, TimeUnit.SECONDS));
    assertTrue(tl.completed.await(3, TimeUnit.SECONDS));
    assertTrue(sl.jobScheduled.await(3, TimeUnit.SECONDS));
  }

  /**
   * 场景：基于 GroupMatcher 查询作业与触发器集合，验证分组匹配能力。
   * 断言：返回集合大小与触发器关联关系符合预期。
   */
  @Test
  public void testMatchersAndQueries() throws Exception {
    JobDetail j1 = JobBuilder.newJob(NoopJob.class).withIdentity("A", "G1").storeDurably().build();
    JobDetail j2 = JobBuilder.newJob(NoopJob.class).withIdentity("B", "G1").storeDurably().build();
    JobDetail j3 = JobBuilder.newJob(NoopJob.class).withIdentity("C", "G2").storeDurably().build();
    quartz.addJob(j1, true);
    quartz.addJob(j2, true);
    quartz.addJob(j3, true);

    Set<JobKey> g1 = quartz.getJobKeys(GroupMatcher.jobGroupEquals("G1"));
    assertEquals(2, g1.size());
    Set<JobKey> containsG = quartz.getJobKeys(GroupMatcher.jobGroupContains("G"));
    assertEquals(3, containsG.size());

    Trigger t1 = TriggerBuilder.newTrigger().withIdentity("T1", "TG1").forJob(j1).startNow().build();
    Trigger t2 = TriggerBuilder.newTrigger().withIdentity("T2", "TG1").forJob(j2).startNow().build();
    quartz.scheduleJob(t1);
    quartz.scheduleJob(t2);

    Set<TriggerKey> tg1 = quartz.getTriggerKeys(GroupMatcher.triggerGroupEquals("TG1"));
    assertEquals(2, tg1.size());

    List<? extends Trigger> triggers = quartz.getTriggersOfJob(j1.getKey());
    assertEquals(1, triggers.size());
  }

  // ===== 作业与监听器实现 =====

  public static class NoopJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
    }
  }

  public static class CountingJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
      TestCounter.increment();
    }
  }

  @PersistJobDataAfterExecution
  public static class PersistingJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
      JobDataMap map = context.getJobDetail().getJobDataMap();
      int c = map.getInt("count");
      map.put("count", c + 1);
      TestCounter.increment();
    }
  }

  @DisallowConcurrentExecution
  public static class NonConcurrentJob implements Job {
    private static final AtomicBoolean inProgress = new AtomicBoolean(false);
    static final AtomicBoolean detectedOverlap = new AtomicBoolean(false);
    static CountDownLatch latch;

    static void reset(CountDownLatch l) {
      inProgress.set(false);
      detectedOverlap.set(false);
      latch = l;
    }

    @Override
    public void execute(JobExecutionContext context) {
      if (!inProgress.compareAndSet(false, true)) {
        detectedOverlap.set(true);
      }
      try {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        if (latch != null) {
          latch.countDown();
        }
      } finally {
        inProgress.set(false);
      }
    }
  }

  static class TestCounter {
    static final AtomicInteger counter = new AtomicInteger(0);
    static CountDownLatch latch;

    static void reset(CountDownLatch l) {
      counter.set(0);
      latch = l;
    }

    static void increment() {
      counter.incrementAndGet();
      if (latch != null) {
        latch.countDown();
      }
    }
  }

  static class TestJobListener implements JobListener {
    final CountDownLatch executed = new CountDownLatch(1);

    @Override
    public String getName() {
      return "TestJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
      executed.countDown();
    }
  }

  static class TestTriggerListener implements TriggerListener {
    final CountDownLatch completed = new CountDownLatch(1);

    @Override
    public String getName() {
      return "TestTriggerListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
      return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
      completed.countDown();
    }
  }

  static class TestSchedulerListener implements SchedulerListener {
    final CountDownLatch jobScheduled = new CountDownLatch(1);

    @Override
    public void jobScheduled(Trigger trigger) {
      jobScheduled.countDown();
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {
    }

    @Override
    public void triggersPaused(String triggerGroup) {
    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
    }

    @Override
    public void triggersResumed(String triggerGroup) {
    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
    }

    @Override
    public void jobPaused(JobKey jobKey) {
    }

    @Override
    public void jobsPaused(String jobGroup) {
    }

    @Override
    public void jobResumed(JobKey jobKey) {
    }

    @Override
    public void jobsResumed(String jobGroup) {
    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
    }

    @Override
    public void schedulerInStandbyMode() {
    }

    @Override
    public void schedulerStarted() {
    }

    @Override
    public void schedulerStarting() {
    }

    @Override
    public void schedulerShutdown() {
    }

    @Override
    public void schedulerShuttingdown() {
    }

    @Override
    public void schedulingDataCleared() {
    }
  }
}
