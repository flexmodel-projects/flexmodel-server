package tech.wetech.flexmodel.infrastructure;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.api.ApiRequestLogService;
import tech.wetech.flexmodel.domain.model.schedule.JobExecutionLogService;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.domain.model.settings.SettingsService;

/**
 * @author cjbi
 */
public class Jobs {

  @Inject
  SettingsService settingsService;
  @Inject
  ApiRequestLogService apiLogService;
  @Inject
  JobExecutionLogService jobExecutionLogService;

  @Scheduled(cron = "0 0 1 * * ?")
  void purgeOldLogs() {
    Settings settings = settingsService.getSettings();
    apiLogService.purgeOldLogs(settings.getLog().getMaxDays());
    jobExecutionLogService.purgeOldLogs(settings.getLog().getMaxDays());
  }

}
