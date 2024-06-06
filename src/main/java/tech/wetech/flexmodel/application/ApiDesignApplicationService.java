package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.application.dto.ApiInfoTreeDTO;
import tech.wetech.flexmodel.domain.model.apidesign.ApiInfo;
import tech.wetech.flexmodel.domain.model.apidesign.ApiInfoService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDesignApplicationService {

  @Inject
  ApiInfoService apiInfoService;

  public List<ApiInfoTreeDTO> findApiInfoTree() {
    List<ApiInfo> list = apiInfoService.findList();
    List<ApiInfoTreeDTO> root = list.stream()
      .filter(apiInfo -> apiInfo.getParentId() == null)
      .map(ApiInfoTreeDTO::new).toList();
    for (ApiInfoTreeDTO treeDTO : root) {
      treeDTO.setChildren(getChildren(treeDTO, list));
    }
    return root;
  }

  private List<ApiInfoTreeDTO> getChildren(ApiInfoTreeDTO treeDTO, List<ApiInfo> list) {
    List<ApiInfoTreeDTO> result = new ArrayList<>();
    for (ApiInfo apiInfo : list) {
      if (treeDTO.getId().equals(apiInfo.getParentId())) {
        ApiInfoTreeDTO dto = new ApiInfoTreeDTO(apiInfo);
        dto.setChildren(getChildren(dto, list));
        result.add(dto);
      }
    }
    return result;
  }

  public ApiInfo createApiInfo(ApiInfo apiInfo) {
    return apiInfoService.create(apiInfo);
  }

  public ApiInfo updateApiInfo(ApiInfo apiInfo) {
    return apiInfoService.update(apiInfo);
  }

  public void deleteApiInfo(String id) {
    apiInfoService.delete(id);
  }

}
