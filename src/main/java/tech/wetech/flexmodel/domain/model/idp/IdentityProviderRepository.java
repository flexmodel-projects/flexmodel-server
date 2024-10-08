package tech.wetech.flexmodel.domain.model.idp;

import tech.wetech.flexmodel.codegen.entity.IdentityProvider;

import java.util.List;

/**
 * @author cjbi
 */
public interface IdentityProviderRepository {

  List<IdentityProvider> findAll();

  IdentityProvider find(String name);

  IdentityProvider save(IdentityProvider identityProvider);

  void delete(String id);

}
