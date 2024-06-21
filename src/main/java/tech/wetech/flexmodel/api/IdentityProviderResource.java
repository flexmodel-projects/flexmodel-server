package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import tech.wetech.flexmodel.application.IdentityProviderApplicationService;
import tech.wetech.flexmodel.domain.model.idp.IdentityProvider;

import java.util.List;

/**
 * @author cjbi
 */
@Path("/api/identity-providers")
public class IdentityProviderResource {

  @Inject
  IdentityProviderApplicationService identityProviderApplicationService;

  @GET
  public List<IdentityProvider> findProviders() {
    return identityProviderApplicationService.findAll();
  }

  @POST
  public IdentityProvider createProvider(IdentityProvider identityProvider) {
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
