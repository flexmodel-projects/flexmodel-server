package tech.wetech.flexmodel.domain.model.idp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class IdentityProviderService {

  @Inject
  IdentityProviderRepository identityProviderRepository;

  public List<IdentityProvider> findAll() {
    return identityProviderRepository.findAll();
  }

  public IdentityProvider save(IdentityProvider identityProvider) {
    return identityProviderRepository.save(identityProvider);
  }

  public void delete(String id) {
    identityProviderRepository.delete(id);
  }

}
