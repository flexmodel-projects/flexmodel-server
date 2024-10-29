package tech.wetech.flexmodel.infrastructrue;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.domain.model.api.ApiRateLimiterHolder;
import tech.wetech.flexmodel.domain.model.settings.SettingsChanged;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class SettingsEventConsumer {


  @ConsumeEvent("settings-changed") // 监听特定地址的事件
  public void consume(SettingsChanged event) {
    log.info("Received message: {}", event.getMessage());
    // 处理事件
    List<String> invalidKeys = new ArrayList<>();
    ApiRateLimiterHolder.getMap().forEach((k, v) -> {
      if (k.endsWith("@default")) {
        invalidKeys.add(k);
      }
    });
    invalidKeys.forEach(ApiRateLimiterHolder::removeApiRateLimiter);
  }

}
