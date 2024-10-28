package tech.wetech.flexmodel.domain.model.settings;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * @author cjbi
 */
@ApplicationScoped
public class SettingsService {

  @Inject
  SettingsRepository settingsRepository;

  public Settings getSettings() {
    return settingsRepository.getSettings();
  }

  public Settings saveSettings(Settings settings) {
    return settingsRepository.saveSettings(settings);
  }

}
