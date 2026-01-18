package dev.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.IdentityProviderApplicationService;
import dev.flexmodel.codegen.entity.IdentityProvider;

import java.util.List;

/**
 * @author cjbi
 */
@Tag(name = "身份源", description = "身份源管理")
@Path("/v1/projects/{projectId}/identity-providers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IdentityProviderResource {

  @Inject
  IdentityProviderApplicationService identityProviderApplicationService;

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        type = SchemaType.ARRAY,
        implementation = IdentityProviderSchema.class
      )
    )
    })
  @Operation(summary = "获取身份源列表")
  @GET
  public List<IdentityProvider> findProviders(@PathParam("projectId") String projectId) {
    return identityProviderApplicationService.findAll(projectId);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = IdentityProviderSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = IdentityProviderSchema.class
      )
    )
    })
  @Operation(summary = "创建身份源")
  @POST
  public IdentityProvider createProvider(@PathParam("projectId") String projectId, IdentityProvider identityProvider) {
    return identityProviderApplicationService.createProvider(projectId, identityProvider);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = IdentityProviderSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = IdentityProviderSchema.class
      )
    )
    })
  @Parameter(name = "name", description = "名称", in = ParameterIn.PATH)
  @Operation(summary = "更新身份源")
  @PUT
  @Path("/{name}")
  public IdentityProvider updateProvider(@PathParam("projectId") String projectId, @PathParam("name") String name, IdentityProvider identityProvider) {
    return identityProviderApplicationService.updateProvider(projectId, identityProvider);
  }

  @Parameter(name = "name", description = "名称", in = ParameterIn.PATH)
  @Operation(summary = "删除身份源")
  @DELETE
  @Path("/{name}")
  public void deleteProvider(@PathParam("projectId") String projectId, @PathParam("name") String name) {
    identityProviderApplicationService.deleteProvider(projectId, name);
  }

  @Schema(
    description = "身份源",
    properties = {
      @SchemaProperty(name = "name", description = "名称，需要唯一"),
      @SchemaProperty(name = "provider", description = "身份源配置", type = SchemaType.OBJECT),
      @SchemaProperty(name = "createdAt", description = "创建日期", readOnly = true),
      @SchemaProperty(name = "updatedAt", description = "更新日期", readOnly = true),
    }
  )
  public static class IdentityProviderSchema extends IdentityProvider {

  }

}
