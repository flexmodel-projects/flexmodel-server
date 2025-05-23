package tech.wetech.flexmodel.infrastructrue;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiRequestLog;
import tech.wetech.flexmodel.domain.model.api.ApiLogRequestService;

/**
 * @author cjbi
 */
@ApplicationScoped
public class LogEventConsumer {

  @Inject
  ApiLogRequestService apiLogService;

  @ConsumeEvent("request.logging") // 监听特定地址的事件
  public void consume(ApiRequestLog apiLog) {
    apiLogService.create(apiLog);
  }

}
