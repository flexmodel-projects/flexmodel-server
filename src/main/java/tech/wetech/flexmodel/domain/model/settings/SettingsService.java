package tech.wetech.flexmodel.domain.model.settings;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * @author cjbi
 */
@ApplicationScoped
public class SettingsService {

  @Inject
  SettingsRepository settingsRepository;

  @CacheResult(cacheName = "settings")
  public Settings getSettings() {
    return settingsRepository.getSettings();
  }

  @CacheInvalidate(cacheName = "settings")
  public Settings saveSettings(Settings settings) {
    return settingsRepository.saveSettings(settings);
  }

}
