package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.Project;
import tech.wetech.flexmodel.codegen.entity.User;
import tech.wetech.flexmodel.domain.model.auth.ProjectService;
import tech.wetech.flexmodel.domain.model.auth.UserService;

import java.util.List;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class AuthApplicationService {

  @Inject
  ProjectService tenantService;

  @Inject
  UserService userService;

  public List<Project> findProjects() {
    return tenantService.findProjects();
  }

  public User login(String username, String password) {
    return userService.login("", username, password);
  }

  public User getUser(String userId) {
    return userService.getUser("", userId);
  }

  public Project findProject(String projectId) {
    return tenantService.findProject(projectId);
  }
}
