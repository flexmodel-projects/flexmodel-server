package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.ApiDefinitionHistory;
import dev.flexmodel.domain.model.api.ApiDefinitionHistoryRepository;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDefinitionHistoryFmRepository implements ApiDefinitionHistoryRepository {

  @Inject
  Session session;

  @Override
  public List<ApiDefinitionHistory> findByApiDefinitionId(String projectId, String apiDefinitionId) {
    return session.dsl().selectFrom(ApiDefinitionHistory.class)
      .where(field(ApiDefinitionHistory::getProjectId).eq(projectId).and(field(ApiDefinitionHistory::getApiDefinitionId).eq(apiDefinitionId)))
      .orderByDesc(ApiDefinitionHistory::getCreatedAt)
      .execute();
  }

  @Override
  public ApiDefinitionHistory save(String projectId, ApiDefinitionHistory apiDefinitionHistory) {
    session.dsl().insertInto(ApiDefinitionHistory.class)
      .values(apiDefinitionHistory)
      .execute();
    return apiDefinitionHistory;
  }

  @Override
  public ApiDefinitionHistory findById(String projectId, String historyId) {
    return session.dsl().selectFrom(ApiDefinitionHistory.class)
      .where(field(ApiDefinitionHistory::getProjectId).eq(projectId).and(field(ApiDefinitionHistory::getId).eq(historyId)))
      .executeOne();
  }
}
