package tech.wetech.flexmodel.infrastructure;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.application.FlowApplicationService;
import tech.wetech.flexmodel.domain.model.flow.dto.StartProcessParamEvent;
import tech.wetech.flexmodel.domain.model.flow.dto.result.StartProcessResult;
import tech.wetech.flexmodel.shared.SessionContextHolder;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class TriggerFlowConsumer {

  @Inject
  FlowApplicationService flowApplicationService;

  @ConsumeEvent("flow.start") // 监听特定地址的事件
  public void consume(StartProcessParamEvent param) {
    SessionContextHolder.setTenantId(param.getTenantId());
    SessionContextHolder.setUserId(param.getUserId());
    StartProcessResult result = flowApplicationService.startProcess(param);
    log.info("flow.start.||startProcessParam={}||result={}", param, result);
  }

}
