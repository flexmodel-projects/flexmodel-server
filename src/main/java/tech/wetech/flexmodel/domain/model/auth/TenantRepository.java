package tech.wetech.flexmodel.domain.model.auth;

import tech.wetech.flexmodel.codegen.entity.Tenant;

import java.util.List;

/**
 * @author cjbi
 */
public interface TenantRepository {

  List<Tenant> findTenants();

}
