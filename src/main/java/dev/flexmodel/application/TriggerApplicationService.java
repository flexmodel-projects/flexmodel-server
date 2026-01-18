package dev.flexmodel.application;

import dev.flexmodel.domain.model.schedule.config.*;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import dev.flexmodel.application.dto.PageDTO;
import dev.flexmodel.application.dto.TriggerDTO;
import dev.flexmodel.application.dto.TriggerPageRequest;
import dev.flexmodel.application.job.ScheduledFlowExecutionJob;
import dev.flexmodel.codegen.entity.FlowDeployment;
import dev.flexmodel.codegen.entity.JobExecutionLog;
import dev.flexmodel.codegen.entity.Trigger;
import dev.flexmodel.domain.model.flow.dto.StartProcessParamEvent;
import dev.flexmodel.domain.model.flow.service.FlowDeploymentService;
import dev.flexmodel.domain.model.schedule.JobExecutionLogService;
import dev.flexmodel.domain.model.schedule.TriggerException;
import dev.flexmodel.domain.model.schedule.TriggerService;
import dev.flexmodel.domain.model.schedule.config.*;
import dev.flexmodel.query.Expressions;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.shared.SessionContextHolder;
import dev.flexmodel.shared.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class TriggerApplicationService {

  @Inject
  TriggerService triggerService;
  @Inject
  FlowDeploymentService flowService;
  @Inject
  Scheduler scheduler;
  @Inject
  EventBus eventBus;
  @Inject
  JobExecutionLogService jobExecutionLogService;

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
    dto.setJobGroup(trigger.getJobGroup());
    dto.setState(trigger.getState());
    dto.setCreatedAt(trigger.getCreatedAt());
    dto.setUpdatedAt(trigger.getUpdatedAt());
    FlowDeployment flowDeployment = flowService.findRecentByFlowModuleId(trigger.getProjectId(), trigger.getJobId());
    if (flowDeployment != null) {
      dto.setJobName(flowDeployment.getFlowName());
    }
    return dto;
  }

  public TriggerDTO findById(String projecjtId, String id) {
    return toTriggerDTO(triggerService.findById(projecjtId, id));
  }

  private String getJobGroup(Trigger trigger, TriggerConfig triggerConfig) {
    if (triggerConfig instanceof EventTriggerConfig eventTriggerConfig) {
      return eventTriggerConfig.getDatasourceName() + "_" + eventTriggerConfig.getModelName();
    } else {
      if (triggerConfig instanceof ScheduledTriggerConfig) {
        FlowDeployment flowDeployment = flowService.findRecentByFlowModuleId(trigger.getProjectId(), trigger.getJobId());
        if (flowDeployment != null) {
          return flowDeployment.getFlowKey();
        }
      }
    }
    return null;
  }

  //  @Transactional
  public Trigger create(String projectId, Trigger trigger) {
    TriggerConfig triggerConfig = JsonUtils.getInstance().convertValue(trigger.getConfig(), TriggerConfig.class);
    trigger.setJobGroup(getJobGroup(trigger, triggerConfig));
    trigger = triggerService.save(projectId, trigger);
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

  //  @Transactional
  public Trigger update(String projectId, Trigger req) {
    Trigger record = triggerService.findById(projectId, req.getId());
    if (record == null) {
      throw new TriggerException("记录不存在");
    }
    if (req.getState() == null) {
      req.setState(record.getState());
    }
    req.setCreatedAt(record.getCreatedAt());
    req.setUpdatedAt(LocalDateTime.now());
    TriggerConfig triggerConfig = JsonUtils.getInstance().convertValue(req.getConfig(), TriggerConfig.class);
    // 规则校验
    triggerConfig.validate();
    req.setJobGroup(getJobGroup(req, triggerConfig));
    Trigger trigger = triggerService.save(projectId, req);

    if (triggerConfig instanceof ScheduledTriggerConfig scheduledTriggerConfig) {
      try {
        // 先删除旧的定时任务
        unscheduleTrigger(req);

        // 只有当 state=true 时才创建新的定时任务
        if (Boolean.TRUE.equals(req.getState())) {
          scheduleTrigger(req, scheduledTriggerConfig);
          log.info("成功更新定时任务: {}", req.getId());
        } else {
          log.info("触发器状态为禁用，已停止定时任务: {}", req.getId());
        }
      } catch (Exception e) {
        log.error("更新定时任务失败: {}", req.getId(), e);
        throw new TriggerException("更新定时任务失败: " + e.getMessage(), e);
      }
    }
    return trigger;
  }

  public void deleteById(String projectId, String id) {
    Trigger record = triggerService.findById(projectId, id);
    if (record != null) {
      // 实现定时任务调度
      try {
        unscheduleTrigger(record);
        log.info("成功删除定时任务: {}", id);
      } catch (Exception e) {
        log.error("删除定时任务失败: {}", id, e);
        throw new TriggerException("删除定时任务失败: " + e.getMessage(), e);
      }
    }
    triggerService.deleteById(projectId, id);
  }

  public PageDTO<TriggerDTO> findPage(String projectId, TriggerPageRequest request) {
    Predicate filter = Expressions.TRUE;
    if (request.getName() != null) {
      filter = filter.and(Expressions.field(Trigger::getName).eq(request.getName()));
    }
    if (request.getJobType() != null) {
      filter = filter.and(Expressions.field(Trigger::getJobType).eq(request.getJobType()));
    }
    if (request.getJobId() != null) {
      filter = filter.and(Expressions.field(Trigger::getJobId).eq(request.getJobId()));
    }
    if (request.getJobGroup() != null) {
      filter = filter.and(Expressions.field(Trigger::getJobGroup).eq(request.getJobGroup()));
    }
    long total = triggerService.count(projectId, filter);
    if (total == 0) {
      return PageDTO.empty();
    }
    List<TriggerDTO> triggers = triggerService.find(projectId, filter, request.getPage(), request.getSize()).stream()
      .map(this::toTriggerDTO)
      .toList();
    return new PageDTO<>(triggers, total);
  }

  public Trigger executeNow(String projectId, String id) {
    try {
      // 查找触发器
      Trigger trigger = triggerService.findById(projectId, id);
      if (trigger == null) {
        throw new TriggerException("触发器不存在: " + id);
      }

      // 验证触发器状态
      if (!Boolean.TRUE.equals(trigger.getState())) {
        throw new TriggerException("触发器已禁用，无法执行: " + id);
      }

      log.info("开始立即执行触发器: triggerId={}, jobId={}, jobType={}",
        trigger.getId(), trigger.getJobId(), trigger.getJobType());

      String projectId2 = SessionContextHolder.getProjectId();

      // 构建启动流程参数
      StartProcessParamEvent startProcessParam = new StartProcessParamEvent();
      startProcessParam.setProjectId(projectId2);
      startProcessParam.setUserId(SessionContextHolder.getUserId());
      startProcessParam.setFlowModuleId(trigger.getJobId());
      startProcessParam.setVariables(Map.of());
      startProcessParam.setStartTime(System.currentTimeMillis());

      JobExecutionLog jobExecutionLog = jobExecutionLogService.recordJobStart(trigger.getId(), trigger.getJobId(), trigger.getJobGroup(),
        trigger.getJobType(), trigger.getName(), trigger.getName(), trigger.getName(), System.currentTimeMillis(),
        System.currentTimeMillis(), startProcessParam, projectId2);

      startProcessParam.setEventId(jobExecutionLog.getId());

      // 直接调用流程应用服务启动流程
      eventBus.send("flow.start", startProcessParam);
      return trigger;
    } catch (TriggerException e) {
      log.error("立即执行触发器失败: {}", id, e);
      throw e;
    } catch (Exception e) {
      log.error("立即执行触发器时发生未知错误: {}", id, e);
      throw new TriggerException("立即执行触发器失败: " + e.getMessage(), e);
    }
  }

  /**
   * 调度定时任务
   */
  private void scheduleTrigger(Trigger trigger, ScheduledTriggerConfig config) throws SchedulerException {
    JobKey jobKey = buildJobKey(trigger);
    // 创建 JobDetail
    JobDetail jobDetail = JobBuilder.newJob(ScheduledFlowExecutionJob.class)
      .withIdentity(jobKey)
      .withDescription(trigger.getDescription())
      .usingJobData("triggerId", trigger.getId())
      .usingJobData("jobGroup", trigger.getJobGroup())
      .usingJobData("jobType", trigger.getJobType())
      .usingJobData("jobId", trigger.getJobId())
      .build();

    // 创建 Trigger
    org.quartz.Trigger quartzTrigger = createQuartzTrigger(trigger, config, jobDetail);

    // 调度任务
    scheduler.scheduleJob(jobDetail, quartzTrigger);

    log.info("已调度定时任务: {}", jobKey);
  }

  /**
   * 取消调度定时任务
   */
  private void unscheduleTrigger(Trigger trigger) throws SchedulerException {
    TriggerKey triggerKey = buildTriggerKey(trigger);
    JobKey jobKey = buildJobKey(trigger);
    if (scheduler.checkExists(triggerKey)) {
      scheduler.unscheduleJob(triggerKey);
      log.info("已取消调度定时任务: {}", triggerKey);
    } else {
      log.warn("定时任务不存在，无需取消: {}", triggerKey);
    }
    if (scheduler.checkExists(jobKey)) {
      scheduler.deleteJob(jobKey);
      log.info("已取消调度定时任务: {}", jobKey);
    } else {
      log.warn("定时任务不存在，无需删除: {}", jobKey);
    }
  }

  private JobKey buildJobKey(Trigger trigger) {
    String triggerId = trigger.getId();
    return JobKey.jobKey("job-" + triggerId, trigger.getJobGroup());
  }

  private TriggerKey buildTriggerKey(Trigger trigger) {
    String triggerId = trigger.getId();
    return TriggerKey.triggerKey("trigger-" + triggerId, trigger.getJobGroup());
  }

  /**
   * 根据配置创建 Quartz Trigger
   */
  private org.quartz.Trigger createQuartzTrigger(Trigger trigger, ScheduledTriggerConfig config, JobDetail jobDetail) {
    TriggerKey triggerKey = buildTriggerKey(trigger);
    TriggerBuilder<org.quartz.Trigger> triggerBuilder = TriggerBuilder.newTrigger()
      .withIdentity(triggerKey)
      .forJob(jobDetail)
      .withDescription("定时触发器: " + triggerKey);

    // 根据不同的配置类型创建不同的触发器
    return switch (config) {
      case CronScheduledTriggerConfig cronConfig -> createCronTrigger(triggerBuilder, cronConfig);
      case IntervalScheduledTriggerConfig intervalConfig -> createIntervalTrigger(triggerBuilder, intervalConfig);
      default -> throw new TriggerException("不支持的定时触发器配置类型: " + config.getClass().getSimpleName());
    };
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

}
