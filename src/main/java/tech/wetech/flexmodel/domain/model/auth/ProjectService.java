package tech.wetech.flexmodel.domain.model.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Project;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ProjectService {

  @Inject
  ProjectRepository tenantRepository;

  public List<Project> findProjects() {
    return tenantRepository.findProjects();
  }

  public Project findProject(String projectId) {
    return tenantRepository.findProject(projectId);
  }

  public Project createProject(Project project) {
    return tenantRepository.save(project);
  }

  public Project updateProject(Project project) {
    return tenantRepository.save(project);
  }

  public void deleteProject(String projectId) {
    tenantRepository.delete(projectId);
  }
}
