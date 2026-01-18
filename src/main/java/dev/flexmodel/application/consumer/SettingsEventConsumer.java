package dev.flexmodel.application.consumer;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.domain.model.api.ApiRateLimiterHolder;
import dev.flexmodel.domain.model.settings.SettingsChanged;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class SettingsEventConsumer {

  public static final String GLOBAL_RATE_LIMIT_KEY = "__DEFAULT";

  @ConsumeEvent("settings-changed") // 监听特定地址的事件
  public void consume(SettingsChanged event) {
    log.info("Received settings message: {}", event.getMessage());
    // 处理事件
    ApiRateLimiterHolder.removeApiRateLimiter(GLOBAL_RATE_LIMIT_KEY);

  }

}
