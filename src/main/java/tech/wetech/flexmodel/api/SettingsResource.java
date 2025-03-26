package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.SettingsApplicationService;
import tech.wetech.flexmodel.domain.model.settings.Settings;

import static tech.wetech.flexmodel.api.Resources.ROOT_PATH;

/**
 * @author cjbi
 */
@Tag(name = "【Flexmodel】设置", description = "系统设置")
@Path(ROOT_PATH + "/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SettingsResource {

  @Inject
  SettingsApplicationService settingsApplicationService;

  @Operation(summary = "获取设置")
  @GET
  public Settings getSettings() {
    return settingsApplicationService.getSettings();
  }

  @Operation(summary = "保存设置")
  @PATCH
  public Settings saveSettings(Settings settings) {
    return settingsApplicationService.saveSettings(settings);
  }

}
