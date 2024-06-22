package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import tech.wetech.flexmodel.domain.model.idp.IdentityProvider;
import tech.wetech.flexmodel.domain.model.idp.IdentityProviderRepository;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class IdentityProviderFmRepository extends BaseFmRepository<IdentityProvider, String> implements IdentityProviderRepository {

  @Override
  public List<IdentityProvider> findAll() {
    return super.findAll();
  }

  @Override
  public IdentityProvider find(String name) {
    return super.findById(name).orElse(null);
  }

  @Override
  public IdentityProvider save(IdentityProvider record) {
    return super.save(record);
  }

  @Override
  public void delete(String id) {
    super.delete(id);
  }
}
