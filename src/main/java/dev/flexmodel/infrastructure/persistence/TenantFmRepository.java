package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Project;
import dev.flexmodel.domain.model.auth.ProjectRepository;
import dev.flexmodel.query.Expressions;
import dev.flexmodel.session.Session;

import java.util.List;

/**
 * 租户
 *
 * @author cjbi
 */
@ApplicationScoped
public class TenantFmRepository implements ProjectRepository {

  @Inject
  Session session;

  @Override
  public List<Project> findProjects() {
    return session.dsl().selectFrom(Project.class).where(Expressions.field(Project::getEnabled).eq(true)).execute().stream()
      .toList();
  }

  @Override
  public Project findProject(String projectId) {
    return session.dsl().selectFrom(Project.class)
        .where(Expressions.field(Project::getId).eq(projectId))
        .executeOne();
  }

  @Override
  public Project save(Project project) {
    session.dsl().mergeInto(Project.class)
        .values(project).execute();
    return project;
  }

  @Override
  public void delete(String projectId) {
    session.dsl().deleteFrom(Project.class)
        .where(Expressions.field(Project::getId).eq(projectId))
        .execute();
  }
}
