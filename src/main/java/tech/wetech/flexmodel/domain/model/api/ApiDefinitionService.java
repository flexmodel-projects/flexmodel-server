package tech.wetech.flexmodel.domain.model.api;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.shared.SessionContextHolder;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDefinitionService {

  @Inject
  ApiDefinitionRepository apiDefinitionRepository;

  @CacheResult(cacheName = "apiDefinitionList")
  public List<ApiDefinition> findList(String tenantId) {
    return apiDefinitionRepository.findByTenantId(tenantId);
  }

  @CacheResult(cacheName = "apiDefinitionList")
  public List<ApiDefinition> findAll() {
    SessionContextHolder.setTenantId(null);
    return apiDefinitionRepository.findAll();
  }

  @CacheInvalidateAll(cacheName = "apiDefinitionList")
  public ApiDefinition create(ApiDefinition apiDefinition) {
    if (apiDefinition.getName() == null || apiDefinition.getName().isEmpty()) {
      throw new ApiDefinitionException("API name must not be null");
    }
    return apiDefinitionRepository.save(apiDefinition);
  }

  @CacheInvalidateAll(cacheName = "apiDefinitionList")
  public ApiDefinition update(ApiDefinition apiDefinition) {
    ApiDefinition older = apiDefinitionRepository.findById(apiDefinition.getId());
    if (older == null) {
      return apiDefinition;
    }
    apiDefinition.setCreatedAt(older.getCreatedAt());
    apiDefinition.setEnabled(older.getEnabled());
    ApiRateLimiterHolder.removeApiRateLimiter(apiDefinition.getMethod() + ":" + apiDefinition.getPath());
    return apiDefinitionRepository.save(apiDefinition);
  }

  @CacheInvalidateAll(cacheName = "apiDefinitionList")
  public void delete(String id) {
    apiDefinitionRepository.delete(id);
    apiDefinitionRepository.deleteByParentId(id);
  }

  public ApiDefinition findApiDefinition(String id) {
    return apiDefinitionRepository.findById(id);
  }
}
