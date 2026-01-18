package dev.flexmodel.infrastructure;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import dev.flexmodel.application.SettingsApplicationService;
import dev.flexmodel.domain.model.api.ApiRequestLogService;
import dev.flexmodel.domain.model.schedule.JobExecutionLogService;
import dev.flexmodel.domain.model.settings.Settings;

/**
 * @author cjbi
 */
public class Jobs {

  @Inject
  SettingsApplicationService settingsApplicationService;
  @Inject
  ApiRequestLogService apiLogService;
  @Inject
  JobExecutionLogService jobExecutionLogService;

  @Scheduled(cron = "0 0 1 * * ?")
  void purgeOldLogs() {
    Settings settings = settingsApplicationService.getSettings();
    apiLogService.purgeOldLogs(settings.getLog().getMaxDays());
    jobExecutionLogService.purgeOldLogs(settings.getLog().getMaxDays());
  }

}
