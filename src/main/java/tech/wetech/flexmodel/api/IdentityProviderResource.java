package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import tech.wetech.flexmodel.application.IdentityProviderApplicationService;
import tech.wetech.flexmodel.codegen.entity.IdentityProvider;

import java.util.List;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Path(BASE_PATH +"/identity-providers")
public class IdentityProviderResource {

  @Inject
  IdentityProviderApplicationService identityProviderApplicationService;

  @GET
  public List<IdentityProvider> findProviders() {
    return identityProviderApplicationService.findAll();
  }

  @POST
  public IdentityProvider createProvider(@Valid IdentityProvider identityProvider) {
    return identityProviderApplicationService.createProvider(identityProvider);
  }

  @PUT
  @Path("/{name}")
  public IdentityProvider updateProvider(@PathParam("name") String name, IdentityProvider identityProvider) {
    return identityProviderApplicationService.updateProvider(identityProvider);
  }

  @DELETE
  @Path("/{name}")
  public void deleteProvider(@PathParam("name") String name) {
    identityProviderApplicationService.deleteProvider(name);
  }

}
