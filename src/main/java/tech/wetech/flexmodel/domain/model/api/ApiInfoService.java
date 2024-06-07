package tech.wetech.flexmodel.domain.model.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiInfoService {

  @Inject
  ApiInfoRepository apiInfoRepository;

  public List<ApiInfo> findList() {
    return apiInfoRepository.findAll();
  }

  public ApiInfo create(ApiInfo apiInfo) {
    if (apiInfo.getName() == null || apiInfo.getName().isEmpty()) {
      throw new ApiInfoException("API name must not be null");
    }
    return apiInfoRepository.save(apiInfo);
  }

  public ApiInfo update(ApiInfo apiInfo) {
    return apiInfoRepository.save(apiInfo);
  }

  public void delete(String id) {
    apiInfoRepository.delete(id);
    apiInfoRepository.deleteByParentId(id);
  }
}
