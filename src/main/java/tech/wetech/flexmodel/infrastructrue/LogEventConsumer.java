package tech.wetech.flexmodel.infrastructrue;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.domain.model.api.ApiLogService;

/**
 * @author cjbi
 */
@ApplicationScoped
public class LogEventConsumer {

  @Inject
  ApiLogService apiLogService;

  @ConsumeEvent("request.logging") // 监听特定地址的事件
  public void consume(ApiLog apiLog) {
    apiLogService.create(apiLog);
  }

}
