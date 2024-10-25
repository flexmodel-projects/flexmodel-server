package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.application.SettingsApplicationService;
import tech.wetech.flexmodel.domain.model.settings.Settings;

/**
 * @author cjbi
 */
@Path("/api/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SettingsResource {

  @Inject
  SettingsApplicationService settingsApplicationService;

  @GET
  public Object getSettings() {
    return settingsApplicationService.getSettings();
  }

  @PATCH
  public Object saveSettings(Settings settings) {
    return settingsApplicationService.saveSettings(settings);
  }

}
