package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.IdentityProvider;
import tech.wetech.flexmodel.domain.model.idp.IdentityProviderRepository;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class IdentityProviderFmRepository implements IdentityProviderRepository {

  @Inject
  Session session;

  @Override
  public List<IdentityProvider> findAll() {
    return session.dsl()
      .selectFrom(IdentityProvider.class)
      .execute();
  }

  @Override
  public IdentityProvider find(String name) {
    return session.dsl()
      .selectFrom(IdentityProvider.class)
      .where(field(IdentityProvider::getName).eq(name))
      .executeOne();
  }

  @Override
  public IdentityProvider save(IdentityProvider record) {

    session.dsl()
      .mergeInto(IdentityProvider.class)
      .values(record)
      .execute();

    return record;

  }

  @Override
  public void delete(String name) {
    session.dsl()
      .deleteFrom(IdentityProvider.class)
      .where(field(IdentityProvider::getName).eq(name))
      .execute();
  }
}
