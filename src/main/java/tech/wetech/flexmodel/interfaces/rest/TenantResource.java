package tech.wetech.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import tech.wetech.flexmodel.application.AuthApplicationService;
import tech.wetech.flexmodel.codegen.entity.Tenant;

import java.util.List;

/**
 * @author cjbi
 */
@Path("/f/tenants")
public class TenantResource {

  @Inject
  AuthApplicationService authApplicationService;

  @GET
  public List<Tenant> findTenants() {
    return authApplicationService.findTenants();
  }

}
