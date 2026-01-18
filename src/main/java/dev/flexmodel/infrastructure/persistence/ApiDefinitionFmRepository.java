package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.ApiDefinition;
import dev.flexmodel.domain.model.api.ApiDefinitionRepository;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDefinitionFmRepository implements ApiDefinitionRepository {

  @Inject
  Session session;

  @Override
  public void deleteByParentId(String projectId, String parentId) {
    session.dsl()
      .deleteFrom(ApiDefinition.class)
      .where(field(ApiDefinition::getProjectId).eq(projectId).and(field(ApiDefinition::getParentId).eq(parentId)))
      .execute();
  }

  @Override
  public ApiDefinition findById(String projectId, String id) {
    return session.dsl()
      .selectFrom(ApiDefinition.class)
      .where(field(ApiDefinition::getProjectId).eq(projectId).and(field(ApiDefinition::getId).eq(id)))
      .executeOne();
  }

  @Override
  public List<ApiDefinition> findAll(String projectId) {
    return session.dsl()
      .selectFrom(ApiDefinition.class)
      .where(field(ApiDefinition::getProjectId).eq(projectId))
      .execute();
  }

  @Override
  public List<ApiDefinition> findByProjectId(String projectId) {
    return session.dsl()
      .selectFrom(ApiDefinition.class)
      .where(field(ApiDefinition::getProjectId).eq(projectId))
      .execute();
  }

  @Override
  public ApiDefinition save(ApiDefinition record) {
    session.dsl().mergeInto(ApiDefinition.class).values(record).execute();
    return record;
  }

  @Override
  public void delete(String projectId, String id) {
    session.dsl().deleteFrom(ApiDefinition.class)
      .where(field(ApiDefinition::getProjectId).eq(projectId).and(field(ApiDefinition::getId).eq(id)))
      .execute();
  }

  @Override
  public Integer count(String projectId) {
    return (int) session.dsl().selectFrom(ApiDefinition.class)
      .where(field(ApiDefinition::getProjectId).eq(projectId))
      .count();
  }

}
