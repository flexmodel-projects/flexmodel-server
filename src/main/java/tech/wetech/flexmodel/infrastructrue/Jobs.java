package tech.wetech.flexmodel.infrastructrue;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.api.ApiLogService;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.domain.model.settings.SettingsService;

/**
 * @author cjbi
 */
public class Jobs {

  @Inject
  SettingsService settingsService;

  @Inject
  ApiLogService apiLogService;

  @Scheduled(cron = "0 0 1 * * ?")
  void purgeOldLogs() {
    Settings settings = settingsService.getSettings();
    apiLogService.purgeOldLogs(settings.getLog().getMaxDays());
  }

}
