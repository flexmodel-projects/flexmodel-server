package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.application.dto.ProjectListRequest;
import tech.wetech.flexmodel.application.dto.ProjectResponse;
import tech.wetech.flexmodel.codegen.entity.Project;
import tech.wetech.flexmodel.codegen.entity.User;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionService;
import tech.wetech.flexmodel.domain.model.auth.ProjectService;
import tech.wetech.flexmodel.domain.model.auth.UserService;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.domain.model.flow.service.FlowDeploymentService;
import tech.wetech.flexmodel.domain.model.storage.StorageService;

import java.util.List;
import java.util.Objects;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class AuthApplicationService {

  @Inject
  ProjectService projectService;

  @Inject
  UserService userService;
  @Inject
  ApiDefinitionService apiDefinitionService;
  @Inject
  FlowDeploymentService flowDeploymentService;
  @Inject
  DatasourceService datasourceService;
  @Inject
  StorageService storageService;

  public List<ProjectResponse> findProjects(ProjectListRequest request) {
    return projectService.findProjects().stream()
      .map(project -> {
          ProjectResponse response = ProjectResponse.fromProject(project);
          if (Objects.equals(request.getIncldue(), "stats")) {
            response.setApiCount(apiDefinitionService.count(project.getId()))
              .setFlowCount(flowDeploymentService.count(project.getId()))
              .setDatasourceCount(datasourceService.count(project.getId()))
              .setStorageCount(storageService.count(project.getId()));
          }
          return response;
        }
      ).toList();
  }

  public User login(String username, String password) {
    return userService.login(username, password);
  }

  public User getUser(String userId) {
    return userService.getUser(userId);
  }

  public Project findProject(String projectId) {
    return projectService.findProject(projectId);
  }
}
