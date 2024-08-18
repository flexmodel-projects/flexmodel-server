package tech.wetech.flexmodel.domain.model.idp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.IdentityProvider;
import tech.wetech.flexmodel.domain.model.idp.provider.Provider;
import tech.wetech.flexmodel.util.JsonUtils;

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

  public IdentityProvider save(IdentityProvider identityProvider) {
    return identityProviderRepository.save(identityProvider);
  }

  public void delete(String id) {
    identityProviderRepository.delete(id);
  }

  public boolean checkToken(String providerName, String token) {
    if (providerName == null || token == null) {
      log.warn("Check token required parameters is null, providerName={}, token={}", providerName, token);
      return false;
    }
    IdentityProvider identityProvider = identityProviderRepository.find(providerName);
    if (identityProvider == null) {
      log.warn("IdentityProvider is null, providerName={}, token={}", providerName, token);
      return false;
    }
    Provider provider = JsonUtils.getInstance().convertValue(identityProvider.getProvider(), Provider.class);
    return provider.checkToken(token);
  }

}
