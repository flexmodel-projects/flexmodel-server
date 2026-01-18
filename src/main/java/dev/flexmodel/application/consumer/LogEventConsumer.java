package dev.flexmodel.application.consumer;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.ApiRequestLog;
import dev.flexmodel.domain.model.api.ApiRequestLogService;

/**
 * @author cjbi
 */
@ApplicationScoped
public class LogEventConsumer {

  @Inject
  ApiRequestLogService apiLogService;

  @ConsumeEvent("request.logging") // 监听特定地址的事件
  public void consume(ApiRequestLog apiLog) {
    apiLogService.create(apiLog);
  }

}
