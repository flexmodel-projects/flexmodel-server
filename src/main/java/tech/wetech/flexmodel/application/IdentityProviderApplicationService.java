package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.IdentityProvider;
import tech.wetech.flexmodel.domain.model.idp.IdentityProviderService;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class IdentityProviderApplicationService {

  @Inject
  IdentityProviderService identityProviderService;

  public List<IdentityProvider> findAll() {
    return identityProviderService.findAll();
  }

  public IdentityProvider createProvider(IdentityProvider identityProvider) {
    return identityProviderService.save(identityProvider);
  }

  public IdentityProvider updateProvider(IdentityProvider identityProvider) {
    return identityProviderService.save(identityProvider);
  }

  public void deleteProvider(String id) {
    identityProviderService.delete(id);
  }

}
