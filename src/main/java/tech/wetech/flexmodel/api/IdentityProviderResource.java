package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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
import tech.wetech.flexmodel.application.IdentityProviderApplicationService;
import tech.wetech.flexmodel.codegen.entity.IdentityProvider;

import java.util.List;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "身份源", description = "身份源管理")
@Path(BASE_PATH + "/identity-providers")
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
  public List<IdentityProvider> findProviders() {
    return identityProviderApplicationService.findAll();
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
  public IdentityProvider createProvider(IdentityProvider identityProvider) {
    return identityProviderApplicationService.createProvider(identityProvider);
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
  public IdentityProvider updateProvider(@PathParam("name") String name, IdentityProvider identityProvider) {
    return identityProviderApplicationService.updateProvider(identityProvider);
  }

  @Parameter(name = "name", description = "名称", in = ParameterIn.PATH)
  @Operation(summary = "删除身份源")
  @DELETE
  @Path("/{name}")
  public void deleteProvider(@PathParam("name") String name) {
    identityProviderApplicationService.deleteProvider(name);
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
