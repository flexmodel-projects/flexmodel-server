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
    StartProcessResult result = flowApplicationService.startProcess(param);
    if (param.getLogId() != null) {
      jobExecutionLogService.recordJobSuccess(param.getLogId(), result.getVariables(), System.currentTimeMillis() - param.getStartTime());
    }
    log.info("flow.start.||startProcessParam={}||result={}", param, result);
  }

}
