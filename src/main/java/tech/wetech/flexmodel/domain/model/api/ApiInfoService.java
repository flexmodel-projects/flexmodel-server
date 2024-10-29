package tech.wetech.flexmodel.domain.model.api;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiInfo;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiInfoService {

  @Inject
  ApiInfoRepository apiInfoRepository;

  @CacheResult(cacheName = "apiInfoList")
  public List<ApiInfo> findList() {
    return apiInfoRepository.findAll();
  }

  @CacheInvalidateAll(cacheName = "apiInfoList")
  public ApiInfo create(ApiInfo apiInfo) {
    if (apiInfo.getName() == null || apiInfo.getName().isEmpty()) {
      throw new ApiInfoException("API name must not be null");
    }
    return apiInfoRepository.save(apiInfo);
  }

  @CacheInvalidateAll(cacheName = "apiInfoList")
  public ApiInfo update(ApiInfo apiInfo) {
    ApiInfo older = apiInfoRepository.findById(apiInfo.getId());
    if (older == null) {
      return apiInfo;
    }
    apiInfo.setCreatedAt(older.getCreatedAt());
    apiInfo.setEnabled(older.getEnabled());
    ApiRateLimiterHolder.removeApiRateLimiter(apiInfo.getMethod() + ":" + apiInfo.getPath());
    return apiInfoRepository.save(apiInfo);
  }

  @CacheInvalidateAll(cacheName = "apiInfoList")
  public ApiInfo updateIgnoreNull(ApiInfo apiInfo) {
    apiInfoRepository.updateIgnoreNull(apiInfo.getId(), apiInfo);
    ApiRateLimiterHolder.removeApiRateLimiter(apiInfo.getMethod() + ":" + apiInfo.getPath());
    return apiInfo;
  }

  @CacheInvalidateAll(cacheName = "apiInfoList")
  public void delete(String id) {
    apiInfoRepository.delete(id);
    apiInfoRepository.deleteByParentId(id);
  }
}
