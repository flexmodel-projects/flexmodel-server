package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.IdentityProviderApplicationService;
import tech.wetech.flexmodel.codegen.entity.IdentityProvider;

import java.util.List;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "身份源", description = "身份源管理")
@Path(BASE_PATH +"/identity-providers")
public class IdentityProviderResource {

  @Inject
  IdentityProviderApplicationService identityProviderApplicationService;

  @Operation(summary = "获取身份源列表")
  @GET
  public List<IdentityProvider> findProviders() {
    return identityProviderApplicationService.findAll();
  }

  @Operation(summary = "创建身份源")
  @POST
  public IdentityProvider createProvider(IdentityProvider identityProvider) {
    return identityProviderApplicationService.createProvider(identityProvider);
  }

  @Operation(summary = "更新身份源")
  @PUT
  @Path("/{name}")
  public IdentityProvider updateProvider(@PathParam("name") String name, IdentityProvider identityProvider) {
    return identityProviderApplicationService.updateProvider(identityProvider);
  }

  @Operation(summary = "删除身份源")
  @DELETE
  @Path("/{name}")
  public void deleteProvider(@PathParam("name") String name) {
    identityProviderApplicationService.deleteProvider(name);
  }

}
