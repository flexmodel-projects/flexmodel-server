package dev.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.domain.model.settings.Settings;
import dev.flexmodel.domain.model.settings.SettingsService;

/**
 * @author cjbi
 */
@ApplicationScoped
public class SettingsApplicationService {

  @Inject
  SettingsService settingsService;

  public Settings getSettings() {
    return settingsService.getSettings();
  }

  public Settings saveSettings(Settings settings) {
    return settingsService.saveSettings(settings);
  }

}
