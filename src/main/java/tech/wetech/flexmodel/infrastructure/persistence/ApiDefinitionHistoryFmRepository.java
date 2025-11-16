package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiDefinitionHistory;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionHistoryRepository;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDefinitionHistoryFmRepository implements ApiDefinitionHistoryRepository {

  @Inject
  Session session;

  @Override
  public List<ApiDefinitionHistory> findByApiDefinitionId(String apiDefinitionId) {
    return session.dsl().selectFrom(ApiDefinitionHistory.class)
      .where(field(ApiDefinitionHistory::getApiDefinitionId).eq(apiDefinitionId))
      .orderByDesc(ApiDefinitionHistory::getCreatedAt)
      .execute();
  }

  @Override
  public ApiDefinitionHistory save(ApiDefinitionHistory apiDefinitionHistory) {
    session.dsl().insertInto(ApiDefinitionHistory.class)
      .values(apiDefinitionHistory)
      .execute();
    return apiDefinitionHistory;
  }

  @Override
  public ApiDefinitionHistory findById(String historyId) {
    return session.dsl().selectFrom(ApiDefinitionHistory.class)
      .where(field(ApiDefinitionHistory::getId).eq(historyId))
      .executeOne();
  }
}
