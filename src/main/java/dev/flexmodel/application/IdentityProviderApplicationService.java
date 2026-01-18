package dev.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.IdentityProvider;
import dev.flexmodel.domain.model.idp.IdentityProviderService;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class IdentityProviderApplicationService {

  @Inject
  IdentityProviderService identityProviderService;

  public List<IdentityProvider> findAll(String projectId) {
    return identityProviderService.findAll();
  }

  public IdentityProvider createProvider(String projectId, IdentityProvider identityProvider) {
    return identityProviderService.create(identityProvider);
  }

  public IdentityProvider updateProvider(String projectId, IdentityProvider identityProvider) {
    return identityProviderService.update(identityProvider);
  }

  public void deleteProvider(String id, String name) {
    identityProviderService.delete(id);
  }

  public IdentityProvider find(String identityProvider) {
    return identityProviderService.find(identityProvider);
  }
}
