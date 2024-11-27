package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.application.dto.ApiInfoTreeDTO;
import tech.wetech.flexmodel.application.dto.GenerateAPIsDTO;
import tech.wetech.flexmodel.codegen.GenerationContext;
import tech.wetech.flexmodel.codegen.GenerationTool;
import tech.wetech.flexmodel.codegen.ModelClass;
import tech.wetech.flexmodel.codegen.entity.ApiInfo;
import tech.wetech.flexmodel.domain.model.api.ApiInfoService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDesignApplicationService {

  @Inject
  ApiInfoService apiInfoService;

  @Inject
  SessionFactory sessionFactory;

  private static final Map<String, ApiDefinitionGenerator> templateMap = new HashMap<>();

  static {
    templateMap.put("list", new ListApiDefinitionGenerator());
    templateMap.put("view", new ViewApiDefinitionGenerator());
    templateMap.put("create", new CreateApiDefinitionGenerator());
    templateMap.put("update", new UpdateApiDefinitionGenerator());
    templateMap.put("delete", new DeleteApiDefinitionGenerator());
    templateMap.put("pagination", new PaginationApiDefinitionGenerator());
  }

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

  public ApiInfo updateApiInfoIgnoreNull(ApiInfo apiInfo) {
    return apiInfoService.updateIgnoreNull(apiInfo);
  }

  public void deleteApiInfo(String id) {
    apiInfoService.delete(id);
  }

  public void generateAPIs(GenerateAPIsDTO dto) {
    List<String> generateAPIs = dto.getGenerateAPIs();
    try (Session session = sessionFactory.createSession(dto.getDatasourceName())) {
      Entity entity = (Entity) session.getModel(dto.getModelName());
      ApiInfo apiFolder = createApiFolder(dto);
      for (String type : generateAPIs) {
        ApiDefinitionGenerator apiDefinitionGenerator = templateMap.get(type);
        if (apiDefinitionGenerator != null) {
          ModelClass modelClass = GenerationTool.buildModelClass(null, dto.getDatasourceName(), entity);
          GenerationContext generationContext = new GenerationContext();
          generationContext.setModelClass(modelClass);
          generationContext.putVariable("idFieldOfPath", dto.getIdFieldOfPath());
          generationContext.putVariable("apiParentId", apiFolder.getId());
          ApiInfo apiInfo = apiDefinitionGenerator.createApiInfo(generationContext);
          apiInfoService.create(apiInfo);
        }
      }
    }
  }

  private ApiInfo createApiFolder(GenerateAPIsDTO dto) {
    ApiInfo folder = new ApiInfo();
    folder.setParentId(null);
    folder.setMethod(null);
    folder.setPath(null);
    folder.setName(dto.getApiFolder());
    folder.setEnabled(false);
    folder.setType("FOLDER");
    return apiInfoService.create(folder);
  }

}
