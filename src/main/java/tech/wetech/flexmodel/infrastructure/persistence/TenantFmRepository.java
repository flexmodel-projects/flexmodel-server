package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Project;
import tech.wetech.flexmodel.domain.model.auth.ProjectRepository;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

/**
 * 租户
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
}
