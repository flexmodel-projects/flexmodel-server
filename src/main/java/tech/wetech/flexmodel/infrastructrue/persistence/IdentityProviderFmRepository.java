package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.dao.IdentityProviderDAO;
import tech.wetech.flexmodel.codegen.entity.IdentityProvider;
import tech.wetech.flexmodel.domain.model.idp.IdentityProviderRepository;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class IdentityProviderFmRepository implements IdentityProviderRepository {

  @Inject
  IdentityProviderDAO identityProviderDAO;

  @Override
  public List<IdentityProvider> findAll() {
    return identityProviderDAO.find(query -> query);
  }

  @Override
  public IdentityProvider find(String name) {
    return identityProviderDAO.findById(name);
  }

  @Override
  public IdentityProvider save(IdentityProvider record) {
    return identityProviderDAO.save(record);
  }

  @Override
  public void delete(String id) {
    identityProviderDAO.deleteById(id);
  }
}
