package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Tenant;
import tech.wetech.flexmodel.domain.model.auth.TenantRepository;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

/**
 * 租户
 * @author cjbi
 */
@ApplicationScoped
public class TenantFmRepository implements TenantRepository {

  @Inject
  Session session;

  @Override
  public List<Tenant> findTenants() {
    return session.dsl().selectFrom(Tenant.class).where(Expressions.field(Tenant::getEnabled).eq(true)).execute().stream()
      .toList();
  }
}
