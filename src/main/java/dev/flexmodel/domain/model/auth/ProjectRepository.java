package dev.flexmodel.domain.model.auth;

import dev.flexmodel.codegen.entity.Project;

import java.util.List;

/**
 * @author cjbi
 */
public interface ProjectRepository {

  List<Project> findProjects();

  Project findProject(String projectId);

  Project save(Project project);

  void delete(String projectId);
}
