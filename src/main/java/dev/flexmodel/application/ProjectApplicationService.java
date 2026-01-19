package dev.flexmodel.application;

import dev.flexmodel.application.dto.ProjectListRequest;
import dev.flexmodel.application.dto.ProjectResponse;
import dev.flexmodel.codegen.entity.Project;
import dev.flexmodel.domain.model.api.ApiDefinitionService;
import dev.flexmodel.domain.model.auth.ProjectService;
import dev.flexmodel.domain.model.connect.DatasourceService;
import dev.flexmodel.domain.model.flow.service.FlowDeploymentService;
import dev.flexmodel.domain.model.storage.StorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ProjectApplicationService {

  @Inject
  ProjectService projectService;
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
            ProjectResponse.ProjectStats projectStats = new ProjectResponse.ProjectStats(
              apiDefinitionService.count(project.getId()),
              flowDeploymentService.count(project.getId()),
              datasourceService.count(project.getId()),
              storageService.count(project.getId())
            );
            response.setStats(projectStats);
          }
          return response;
        }
      ).toList();
  }

  public Project findProject(String projectId) {
    return projectService.findProject(projectId);
  }

  public Project createProject(Project project) {
    return projectService.createProject(project);
  }

  public Project updateProject(Project project) {
    return projectService.updateProject(project);
  }

  public void deleteProject(String projectId) {
    projectService.deleteProject(projectId);
  }

}
