package dev.flexmodel.domain.model.idp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.codegen.entity.IdentityProvider;

import java.util.List;


/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class IdentityProviderService {

  @Inject
  IdentityProviderRepository identityProviderRepository;

  public List<IdentityProvider> findAll() {
    return identityProviderRepository.findAll();
  }

  public IdentityProvider create(IdentityProvider identityProvider) {
    return identityProviderRepository.save(identityProvider);
  }

  public IdentityProvider update(IdentityProvider identityProvider) {
    IdentityProvider older = identityProviderRepository.find(identityProvider.getName());
    if (older == null) {
      return identityProvider;
    }
    identityProvider.setCreatedAt(older.getCreatedAt());
    return identityProviderRepository.save(identityProvider);
  }

  public void delete(String id) {
    identityProviderRepository.delete(id);
  }


  public IdentityProvider find(String providerName) {
    return identityProviderRepository.find(providerName);
  }
}
