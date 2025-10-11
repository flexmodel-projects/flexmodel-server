package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.Tenant;
import tech.wetech.flexmodel.codegen.entity.User;
import tech.wetech.flexmodel.domain.model.auth.TenantService;
import tech.wetech.flexmodel.domain.model.auth.UserService;

import java.util.List;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class AuthApplicationService {

  @Inject
  TenantService tenantService;

  @Inject
  UserService userService;

  public List<Tenant> findTenants() {
    return tenantService.findTenants();
  }

  public User login(String username, String password) {
    return userService.login(username, password);
  }

  public User getUser(String userId) {
    return userService.getUser(userId);
  }

}
