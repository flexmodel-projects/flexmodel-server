package dev.flexmodel.domain.model.api;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.ApiDefinition;
import dev.flexmodel.codegen.entity.ApiDefinitionHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDefinitionService {

  @Inject
  ApiDefinitionRepository apiDefinitionRepository;

  @Inject
  ApiDefinitionHistoryRepository apiDefinitionHistoryRepository;

  @Inject
  EventBus eventBus;

  @CacheResult(cacheName = "apiDefinitionList")
  public List<ApiDefinition> findList(String projectId) {
    return apiDefinitionRepository.findByProjectId(projectId);
  }

  public Integer count(String projectId) {
    return apiDefinitionRepository.count(projectId);
  }

  @CacheResult(cacheName = "apiDefinitionList")
  public List<ApiDefinition> findAll(String projectId) {
    return apiDefinitionRepository.findAll(projectId);
  }

  public List<ApiDefinitionHistory> findApiDefinitionHistories(String projectId, String apiDefinitionId) {
    return apiDefinitionHistoryRepository.findByApiDefinitionId(projectId, apiDefinitionId);
  }

  public ApiDefinitionHistory saveApiDefinitionHistory(ApiDefinitionHistory apiDefinitionHistory) {
    return apiDefinitionHistoryRepository.save(apiDefinitionHistory.getProjectId(), apiDefinitionHistory);
  }

  @CacheInvalidateAll(cacheName = "apiDefinitionList")
  public ApiDefinition create(ApiDefinition apiDefinition) {
    if (apiDefinition.getName() == null || apiDefinition.getName().isEmpty()) {
      throw new ApiDefinitionException("API name must not be null");
    }
    ApiDefinition definition = apiDefinitionRepository.save(apiDefinition);
    eventBus.publish("api.changed", new ApiDefinitionChangedEvent(definition));
    return definition;
  }

  @CacheInvalidateAll(cacheName = "apiDefinitionList")
  public ApiDefinition update(ApiDefinition apiDefinition) {
    ApiDefinition older = apiDefinitionRepository.findById(apiDefinition.getProjectId(), apiDefinition.getId());
    if (older == null) {
      return apiDefinition;
    }
    apiDefinition.setCreatedAt(older.getCreatedAt());
    apiDefinition.setUpdatedAt(LocalDateTime.now());
    ApiRateLimiterHolder.removeApiRateLimiter(apiDefinition.getMethod() + ":" + apiDefinition.getPath());
    ApiDefinition definition = apiDefinitionRepository.save(apiDefinition);
    eventBus.publish("api.changed", new ApiDefinitionChangedEvent(definition));
    return definition;
  }

  @CacheInvalidateAll(cacheName = "apiDefinitionList")
  public void delete(String projectId, String id) {
    apiDefinitionRepository.delete(projectId, id);
    apiDefinitionRepository.deleteByParentId(projectId, id);
  }

  public ApiDefinition findApiDefinition(String projectId, String id) {
    return apiDefinitionRepository.findById(projectId, id);
  }

  public ApiDefinitionHistory findApiDefinitionHistory(String projectId, String historyId) {
    return apiDefinitionHistoryRepository.findById(projectId, historyId);
  }
}
