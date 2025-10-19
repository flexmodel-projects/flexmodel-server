package tech.wetech.flexmodel.infrastructure;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.application.FlowApplicationService;
import tech.wetech.flexmodel.domain.model.flow.dto.param.StartProcessParam;
import tech.wetech.flexmodel.domain.model.flow.dto.result.StartProcessResult;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class TriggerFlowConsumer {

  @Inject
  FlowApplicationService flowApplicationService;

  @ConsumeEvent("flow.start") // 监听特定地址的事件
  public void consume(StartProcessParam param) {
    StartProcessResult result = flowApplicationService.startProcess(param);
    log.info("flow.start.||startProcessParam={}||result={}", param, result);
  }

}
