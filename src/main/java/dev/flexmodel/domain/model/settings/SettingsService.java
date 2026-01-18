package dev.flexmodel.domain.model.settings;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * @author cjbi
 */
@ApplicationScoped
public class SettingsService {

  @Inject
  SettingsRepository settingsRepository;

  @Inject
  EventBus eventBus;

  @CacheResult(cacheName = "settings")
  public Settings getSettings() {
    return settingsRepository.getSettings();
  }

  @CacheInvalidateAll(cacheName = "settings")
  public Settings saveSettings(Settings settings) {
    try {
      return settingsRepository.saveSettings(settings);
    } finally {
      eventBus.publish("settings.changed", new SettingsChanged(settings));
    }
  }

}
