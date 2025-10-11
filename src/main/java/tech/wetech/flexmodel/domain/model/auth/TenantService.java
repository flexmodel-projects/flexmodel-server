package tech.wetech.flexmodel.domain.model.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Tenant;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class TenantService {

  @Inject
  TenantRepository tenantRepository;

  public List<Tenant> findTenants() {
    return tenantRepository.findTenants();
  }

}
