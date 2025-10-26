package tech.wetech.flexmodel.application;

import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.JobExecutionLog;
import tech.wetech.flexmodel.codegen.entity.Trigger;
import tech.wetech.flexmodel.domain.model.flow.dto.StartProcessParamEvent;
import tech.wetech.flexmodel.domain.model.schedule.JobExecutionLogService;
import tech.wetech.flexmodel.domain.model.schedule.TriggerService;
import tech.wetech.flexmodel.event.ChangedEvent;
import tech.wetech.flexmodel.event.EventListener;
import tech.wetech.flexmodel.event.EventType;
import tech.wetech.flexmodel.event.PreChangeEvent;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.shared.SessionContextHolder;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class TriggerDataChangedEventListener implements EventListener {

  @Inject
  TriggerService triggerService;
  @Inject
  JobExecutionLogService jobExecutionLogService;
  @Inject
  EventBus eventBus;

  private final Map<String, String> beforeMutationTypeMap = Map.of(
    "delete", "PRE_DELETE",
    "update", "PRE_UPDATE",
    "create", "PRE_INSERT"
  );

  private final Map<String, String> afterMutationTypeMap = Map.of(
    "delete", "DELETED",
    "update", "UPDATED",
    "create", "INSERTED"
  );

  @Override
  public void onPreChange(PreChangeEvent event) {
    String groupName = event.getSchemaName() + "_" + event.getModelName();
    // 最多支持触发100个事件
    List<Trigger> triggers = triggerService.find(
      Expressions.field(Trigger::getJobGroup).eq(groupName)
        .and(Expressions.field(Trigger::getState).eq(true)), 1, 100);

    for (Trigger trigger : triggers) {
      @SuppressWarnings("unchecked")
      Map<String, Object> config = (Map<String, Object>) trigger.getConfig();
      String triggerTiming = (String) config.get("triggerTiming");
      if (triggerTiming.equals("before")) {
        @SuppressWarnings("unchecked")
        List<String> mutationTypes = (List<String>) config.get("mutationTypes");
        for (String mutationType : mutationTypes) {
          String eventType = beforeMutationTypeMap.get(mutationType);
          if (eventType.equals(event.getEventType())) {
            log.info("触发前置定时任务: triggerId={}, eventType={}, schemaName={}, modelName={}",
              trigger.getId(), eventType, event.getSchemaName(), event.getModelName());

            // 记录事件触发日志
            String logId = recordEventTriggerLog(trigger, event, "PRE_CHANGE", mutationType);

            // 构建启动流程参数
            StartProcessParamEvent startProcessParam = new StartProcessParamEvent();
            startProcessParam.setTenantId(SessionContextHolder.getTenantId());
            startProcessParam.setUserId(SessionContextHolder.getUserId());
            startProcessParam.setFlowModuleId(trigger.getJobId());
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = event.getNewData();
            startProcessParam.setVariables(variables);
            startProcessParam.setLogId(logId);
            startProcessParam.setStartTime(System.currentTimeMillis());

            eventBus.send("flow.start", startProcessParam);
          }
        }
      }
    }
  }

  @Override
  public void onChanged(ChangedEvent event) {
    String groupName = event.getSchemaName() + "_" + event.getModelName();
    // 最多支持触发100个事件
    List<Trigger> triggers = triggerService.find(
      Expressions.field(Trigger::getJobGroup).eq(groupName)
        .and(Expressions.field(Trigger::getState).eq(true)), 1, 100);

    for (Trigger trigger : triggers) {
      @SuppressWarnings("unchecked")
      Map<String, Object> config = (Map<String, Object>) trigger.getConfig();
      String triggerTiming = (String) config.get("triggerTiming");
      if (triggerTiming.equals("after")) {
        @SuppressWarnings("unchecked")
        List<String> mutationTypes = (List<String>) config.get("mutationTypes");
        for (String mutationType : mutationTypes) {
          String eventType = afterMutationTypeMap.get(mutationType);
          if (eventType.equals(event.getEventType())) {
            log.info("触发后置定时任务: triggerId={}, eventType={}, schemaName={}, modelName={}",
              trigger.getId(), eventType, event.getSchemaName(), event.getModelName());

            // 记录事件触发日志
            String logId = recordEventTriggerLog(trigger, event, "POST_CHANGE", mutationType);

            // 构建启动流程参数
            StartProcessParamEvent startProcessParam = new StartProcessParamEvent();
            startProcessParam.setTenantId(SessionContextHolder.getTenantId());
            startProcessParam.setUserId(SessionContextHolder.getUserId());
            startProcessParam.setFlowModuleId(trigger.getJobId());
            startProcessParam.setLogId(logId);
            startProcessParam.setStartTime(System.currentTimeMillis());

            @SuppressWarnings("unchecked")
            Map<String, Object> variables = JsonUtils.getInstance().convertValue(event.getNewData(), Map.class);
            startProcessParam.setVariables(variables);
            eventBus.send("flow.start", startProcessParam);
          }
        }
      }
    }
  }

  @Override
  public boolean supports(String eventType) {
    return !eventType.equals(EventType.PRE_QUERY.getValue());
  }

  /**
   * 记录事件触发日志
   */
  private String recordEventTriggerLog(Trigger trigger, Object event, String triggerPhase, String mutationType) {
    try {
      // 构建输入数据
      Map<String, Object> inputData = Map.of(
        "triggerId", trigger.getId(),
        "triggerName", trigger.getName(),
        "triggerPhase", triggerPhase,
        "mutationType", mutationType,
        "eventData", event,
        "triggerTime", System.currentTimeMillis()
      );

      // 记录事件触发日志
      JobExecutionLog jobExecutionLog = jobExecutionLogService.recordJobStart(
        trigger.getId(),
        trigger.getJobId(),
        trigger.getJobGroup(),
        trigger.getJobType(),
        trigger.getName(),
        "EventTrigger",
        "EventTriggerInstance",
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        inputData,
        trigger.getTenantId()
      );

      log.debug("已记录事件触发日志: triggerId={}, phase={}, mutationType={}",
        trigger.getId(), triggerPhase, mutationType);
      return jobExecutionLog.getId();
    } catch (Exception e) {
      log.error("记录事件触发日志失败: triggerId={}, phase={}", trigger.getId(), triggerPhase, e);
    }
    return null;
  }
}
