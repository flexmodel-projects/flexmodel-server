package dev.flexmodel.interfaces.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.SettingsApplicationService;
import dev.flexmodel.shared.FlexmodelConfig;

import java.util.Map;

/**
 * @author cjbi
 */
@Tag(name = "系统", description = "系统信息")
@Slf4j
@Path("/v1/global")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GlobalResource {

  @Inject
  SettingsApplicationService settingsApplicationService;

  @Inject
  FlexmodelConfig config;

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        properties = {
          @SchemaProperty(name = "env", description = "环境变量"),
          @SchemaProperty(name = "properties", description = "配置属性"),
          @SchemaProperty(name = "application", description = "应用程序配置"),
          @SchemaProperty(name = "settings", description = "系统设置"),
        }
      )
    )
    })
  @Operation(summary = "获取系统配置")
  @GET
  @Path("/profile")
  @PermitAll
  public Map<String, Object> getProfile() {
    return Map.of("settings", settingsApplicationService.getSettings(),
    "apiRootPath", config.apiRootPath()
    );
  }

}
