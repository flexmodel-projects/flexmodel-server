package dev.flexmodel.domain.model.auth;

import dev.flexmodel.shared.SessionContextHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Project;
import org.apache.commons.lang3.StringUtils;

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
    if (!StringUtils.isBlank(project.getId()) && findProject(project.getId()) != null) {
      throw new IllegalArgumentException("项目ID已经存在");
    }
    if (StringUtils.isBlank(project.getId())) {
      project.setId(null);
    }
    project.setOwnerId(SessionContextHolder.getUserId());
    return tenantRepository.save(project);
  }

  public Project updateProject(Project project) {
    if ("default".equals(project.getId())) {
      throw new IllegalArgumentException("默认项目不能修改");
    }
    Project existingProject = findProject(project.getId());
    if (existingProject == null) {
      throw new IllegalArgumentException("项目不存在");
    }
    project.setCreatedAt(existingProject.getCreatedAt());
    project.setCreatedBy(existingProject.getCreatedBy());
    project.setOwnerId(existingProject.getOwnerId());
    return tenantRepository.save(project);
  }

  public void deleteProject(String projectId) {
    if ("default".equals(projectId)) {
      throw new IllegalArgumentException("默认项目不能删除");
    }
    tenantRepository.delete(projectId);
  }
}
