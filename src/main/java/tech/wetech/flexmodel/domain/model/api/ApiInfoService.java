package tech.wetech.flexmodel.domain.model.api;

import io.quarkus.cache.CacheInvalidate;
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

  @CacheInvalidate(cacheName = "apiInfoList")
  public ApiInfo create(ApiInfo apiInfo) {
    if (apiInfo.getName() == null || apiInfo.getName().isEmpty()) {
      throw new ApiInfoException("API name must not be null");
    }
    return apiInfoRepository.save(apiInfo);
  }

  @CacheInvalidate(cacheName = "apiInfoList")
  public ApiInfo update(ApiInfo apiInfo) {
    ApiInfo older = apiInfoRepository.findById(apiInfo.getId());
    if (older == null) {
      return apiInfo;
    }
    apiInfo.setCreatedAt(older.getCreatedAt());
    apiInfo.setEnabled(older.getEnabled());
    return apiInfoRepository.save(apiInfo);
  }

  @CacheInvalidate(cacheName = "apiInfoList")
  public ApiInfo updateIgnoreNull(ApiInfo apiInfo) {
    apiInfoRepository.updateIgnoreNull(apiInfo.getId(), apiInfo);
    return apiInfo;
  }

  @CacheInvalidate(cacheName = "apiInfoList")
  public void delete(String id) {
    apiInfoRepository.delete(id);
    apiInfoRepository.deleteByParentId(id);
  }
}
