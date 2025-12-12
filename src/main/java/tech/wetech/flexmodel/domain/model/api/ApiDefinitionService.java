package tech.wetech.flexmodel.domain.model.api;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.codegen.entity.ApiDefinitionHistory;
import tech.wetech.flexmodel.shared.SessionContextHolder;

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
  public List<ApiDefinition> findList(String tenantId) {
    return apiDefinitionRepository.findByTenantId(tenantId);
  }

  @CacheResult(cacheName = "apiDefinitionList")
  public List<ApiDefinition> findAll() {
    SessionContextHolder.setTenantId(null);
    return apiDefinitionRepository.findAll();
  }

  public List<ApiDefinitionHistory> findApiDefinitionHistories(String apiDefinitionId) {
    return apiDefinitionHistoryRepository.findByApiDefinitionId(apiDefinitionId);
  }

  public ApiDefinitionHistory saveApiDefinitionHistory(ApiDefinitionHistory apiDefinitionHistory) {
    return apiDefinitionHistoryRepository.save(apiDefinitionHistory);
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
    ApiDefinition older = apiDefinitionRepository.findById(apiDefinition.getId());
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
  public void delete(String id) {
    apiDefinitionRepository.delete(id);
    apiDefinitionRepository.deleteByParentId(id);
  }

  public ApiDefinition findApiDefinition(String id) {
    return apiDefinitionRepository.findById(id);
  }

  public ApiDefinitionHistory findApiDefinitionHistory(String historyId) {
    return apiDefinitionHistoryRepository.findById(historyId);
  }
}
