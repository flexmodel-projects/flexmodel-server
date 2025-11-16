package tech.wetech.flexmodel.application.consumer;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiRequestLog;
import tech.wetech.flexmodel.domain.model.api.ApiRequestLogService;

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
