package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.application.dto.TriggerDTO;
import tech.wetech.flexmodel.application.job.ScheduledFlowExecutionJob;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.codegen.entity.Trigger;
import tech.wetech.flexmodel.domain.model.flow.service.FlowDeploymentService;
import tech.wetech.flexmodel.domain.model.trigger.TriggerException;
import tech.wetech.flexmodel.domain.model.trigger.TriggerService;
import tech.wetech.flexmodel.domain.model.trigger.config.*;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class ScheduleApplicationService {

  @Inject
  TriggerService triggerService;
  @Inject
  FlowDeploymentService flowService;
  @Inject
  Scheduler scheduler;

  private TriggerDTO toTriggerDTO(Trigger trigger) {
    if (trigger == null) {
      return null;
    }
    TriggerDTO dto = new TriggerDTO();
    dto.setId(trigger.getId());
    dto.setName(trigger.getName());
    dto.setDescription(trigger.getDescription());
    dto.setType(trigger.getType());
    dto.setConfig(trigger.getConfig());
    dto.setJobId(trigger.getJobId());
    dto.setJobType(trigger.getJobType());
    dto.setState(trigger.getState());
    dto.setCreatedAt(trigger.getCreatedAt());
    dto.setUpdatedAt(trigger.getUpdatedAt());
    FlowDeployment flowDeployment = flowService.findRecentByFlowModuleId(trigger.getJobId());
    if (flowDeployment != null) {
      dto.setJobName(flowDeployment.getFlowName());
    }
    return dto;
  }

  public TriggerDTO findById(String id) {
    return toTriggerDTO(triggerService.findById(id));
  }

  public Trigger create(Trigger trigger) {
    trigger = triggerService.save(trigger);
    TriggerConfig triggerConfig = JsonUtils.getInstance().convertValue(trigger.getConfig(), TriggerConfig.class);
    // 规则校验
    triggerConfig.validate();
    if (triggerConfig instanceof ScheduledTriggerConfig scheduledTriggerConfig) {
      // 实现定时任务调度
      try {
        scheduleTrigger(trigger, scheduledTriggerConfig);
        log.info("成功创建定时任务: {}", trigger.getId());
      } catch (Exception e) {
        log.error("创建定时任务失败: {}", trigger.getId(), e);
        throw new TriggerException("创建定时任务失败: " + e.getMessage(), e);
      }
    }
    return trigger;
  }

  public Trigger update(Trigger req) {
    TriggerConfig triggerConfig = JsonUtils.getInstance().convertValue(req.getConfig(), TriggerConfig.class);
    // 规则校验
    triggerConfig.validate();
    if (triggerConfig instanceof ScheduledTriggerConfig scheduledTriggerConfig) {
      // 实现定时任务调度
      try {
        // 先删除旧的定时任务
        unscheduleTrigger(req.getId());
        // 创建新的定时任务
        scheduleTrigger(req, scheduledTriggerConfig);
        log.info("成功更新定时任务: {}", req.getId());
      } catch (Exception e) {
        log.error("更新定时任务失败: {}", req.getId(), e);
        throw new TriggerException("更新定时任务失败: " + e.getMessage(), e);
      }
    }
    Trigger record = findById(req.getId());
    if (record == null) {
      throw new TriggerException("记录不存在");
    }
    return triggerService.save(req);
  }

  public void deleteById(String id) {
    Trigger record = findById(id);
    if (record != null) {
      // 实现定时任务调度
      try {
        unscheduleTrigger(id);
        log.info("成功删除定时任务: {}", id);
      } catch (Exception e) {
        log.error("删除定时任务失败: {}", id, e);
        throw new TriggerException("删除定时任务失败: " + e.getMessage(), e);
      }
    }
    triggerService.deleteById(id);
  }

  public PageDTO<TriggerDTO> findPage(String name, Integer page, Integer size) {
    Predicate filter = Expressions.TRUE;
    if (name != null) {
      filter = filter.and(Expressions.field("name").eq(name));
    }
    long total = triggerService.count(filter);
    if (total == 0) {
      return PageDTO.empty();
    }
    List<TriggerDTO> triggers = triggerService.find(filter, page, size).stream()
        .map(this::toTriggerDTO)
        .toList();
    return new PageDTO<>(triggers, total);
  }

  public void executeNow(String id) {

  }

  /**
   * 调度定时任务
   */
  private void scheduleTrigger(Trigger trigger, ScheduledTriggerConfig config) throws SchedulerException {
    String triggerId = trigger.getId();

    // 创建 JobDetail
    JobDetail jobDetail = JobBuilder.newJob(ScheduledFlowExecutionJob.class)
        .withIdentity("job-" + triggerId, "trigger-group")
        .withDescription(trigger.getDescription())
        .usingJobData("triggerId", triggerId)
        .usingJobData("flowModuleId", trigger.getJobId()) // 这里可能需要根据实际业务逻辑调整
        .build();

    // 创建 Trigger
    org.quartz.Trigger quartzTrigger = createQuartzTrigger(triggerId, config, jobDetail);

    // 调度任务
    scheduler.scheduleJob(jobDetail, quartzTrigger);

    log.info("已调度定时任务: {}", triggerId);
  }

  /**
   * 取消调度定时任务
   */
  private void unscheduleTrigger(String triggerId) throws SchedulerException {
    JobKey jobKey = JobKey.jobKey("job-" + triggerId, "trigger-group");

    if (scheduler.checkExists(jobKey)) {
      scheduler.deleteJob(jobKey);
      log.info("已取消调度定时任务: {}", triggerId);
    } else {
      log.warn("定时任务不存在，无需删除: {}", triggerId);
    }
  }

  /**
   * 根据配置创建 Quartz Trigger
   */
  private org.quartz.Trigger createQuartzTrigger(String triggerId, ScheduledTriggerConfig config, JobDetail jobDetail) {
    TriggerBuilder<org.quartz.Trigger> triggerBuilder = TriggerBuilder.newTrigger()
        .withIdentity("trigger-" + triggerId, "trigger-group")
        .forJob(jobDetail)
        .withDescription("定时触发器: " + triggerId);

    // 根据不同的配置类型创建不同的触发器
    if (config instanceof CronScheduledTriggerConfig cronConfig) {
      return createCronTrigger(triggerBuilder, cronConfig);
    } else if (config instanceof IntervalScheduledTriggerConfig intervalConfig) {
      return createIntervalTrigger(triggerBuilder, intervalConfig);
    } else if (config instanceof DailyTimeIntervalScheduledTriggerConfig dailyConfig) {
      return createDailyTimeIntervalTrigger(triggerBuilder, dailyConfig);
    } else {
      throw new TriggerException("不支持的定时触发器配置类型: " + config.getClass().getSimpleName());
    }
  }

  /**
   * 创建 Cron 触发器
   */
  private org.quartz.Trigger createCronTrigger(TriggerBuilder<org.quartz.Trigger> triggerBuilder,
                                               CronScheduledTriggerConfig config) {
    return triggerBuilder
        .withSchedule(CronScheduleBuilder.cronSchedule(config.getCronExpression())
            .withMisfireHandlingInstructionFireAndProceed())
        .startNow()
        .build();
  }

  /**
   * 创建间隔触发器
   */
  private org.quartz.Trigger createIntervalTrigger(TriggerBuilder<org.quartz.Trigger> triggerBuilder,
                                                   IntervalScheduledTriggerConfig config) {
    SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule();

    // 设置间隔时间
    switch (config.getIntervalUnit().toLowerCase()) {
      case "second" -> scheduleBuilder.withIntervalInSeconds(config.getInterval());
      case "minute" -> scheduleBuilder.withIntervalInMinutes(config.getInterval());
      case "hour" -> scheduleBuilder.withIntervalInHours(config.getInterval());
      case "day" -> scheduleBuilder.withIntervalInHours(config.getInterval() * 24);
      case "week" -> scheduleBuilder.withIntervalInHours(config.getInterval() * 24 * 7);
      case "month" -> scheduleBuilder.withIntervalInHours(config.getInterval() * 24 * 30);
      case "year" -> scheduleBuilder.withIntervalInHours(config.getInterval() * 24 * 365);
      default -> throw new TriggerException("不支持的间隔时间单位: " + config.getIntervalUnit());
    }

    // 设置重复次数
    if (config.getRepeatCount() != null && config.getRepeatCount() > 0) {
      scheduleBuilder.withRepeatCount(config.getRepeatCount() - 1); // Quartz 的重复次数不包括第一次执行
    } else {
      scheduleBuilder.repeatForever();
    }

    return triggerBuilder
        .withSchedule(scheduleBuilder.withMisfireHandlingInstructionFireNow())
        .startNow()
        .build();
  }

  /**
   * 创建每日时间间隔触发器
   */
  private org.quartz.Trigger createDailyTimeIntervalTrigger(TriggerBuilder<org.quartz.Trigger> triggerBuilder,
                                                           DailyTimeIntervalScheduledTriggerConfig config) {
    DailyTimeIntervalScheduleBuilder scheduleBuilder = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule();
    // 设置时间间隔
    switch (config.getIntervalUnit().toLowerCase()) {
      case "second" -> scheduleBuilder.withIntervalInSeconds(config.getInterval());
      case "minute" -> scheduleBuilder.withIntervalInMinutes(config.getInterval());
      case "hour" -> scheduleBuilder.withIntervalInHours(config.getInterval());
      default -> throw new TriggerException("不支持的间隔时间单位: " + config.getIntervalUnit());
    }

    // 设置开始和结束时间
    if (config.getStartTime() != null) {
      Date startTime = Date.from(config.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
      triggerBuilder.startAt(startTime);
    } else {
      triggerBuilder.startNow();
    }

    if (config.getEndTime() != null) {
      Date endTime = Date.from(config.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
      triggerBuilder.endAt(endTime);
    }

    // 设置星期几
    if (config.getDaysOfWeek() != null && !config.getDaysOfWeek().isEmpty()) {
      // 将 1-7 转换为 Quartz 的 1-7 (周日=1, 周一=2, ..., 周六=7)
      scheduleBuilder.onDaysOfTheWeek(config.getDaysOfWeek());
    }

    return triggerBuilder
        .withSchedule(scheduleBuilder.withMisfireHandlingInstructionFireAndProceed())
        .build();
  }
}
