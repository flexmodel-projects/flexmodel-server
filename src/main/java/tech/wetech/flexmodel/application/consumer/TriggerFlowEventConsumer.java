package tech.wetech.flexmodel.application.consumer;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.application.FlowApplicationService;
import tech.wetech.flexmodel.domain.model.flow.dto.StartProcessParamEvent;
import tech.wetech.flexmodel.domain.model.flow.dto.result.StartProcessResult;
import tech.wetech.flexmodel.domain.model.schedule.JobExecutionLogService;
import tech.wetech.flexmodel.shared.SessionContextHolder;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class TriggerFlowEventConsumer {

  @Inject
  FlowApplicationService flowApplicationService;

  @Inject
  JobExecutionLogService jobExecutionLogService;

  @ConsumeEvent("flow.start") // 监听特定地址的事件
  public void consume(StartProcessParamEvent param) {
    SessionContextHolder.setTenantId(param.getTenantId());
    SessionContextHolder.setUserId(param.getUserId());
    StartProcessResult result = null;
    try {
      result = flowApplicationService.startProcess(param);
      log.info("flow.start.||startProcessParam={}||result={}", param, result);
    } catch (Exception e) {
      if (param.getEventId() != null) {
        jobExecutionLogService.recordJobFailure(param.getEventId(), e.getMessage(), e.getStackTrace(), System.currentTimeMillis() - param.getStartTime());
      }
    } finally {
      if (param.getEventId() != null) {
        jobExecutionLogService.recordJobSuccess(param.getEventId(), result, System.currentTimeMillis() - param.getStartTime());
      }
    }

  }

}
