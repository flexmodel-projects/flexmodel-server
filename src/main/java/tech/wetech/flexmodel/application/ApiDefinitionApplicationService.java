package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.application.dto.ApiDefinitionTreeDTO;
import tech.wetech.flexmodel.application.dto.GenerateAPIsDTO;
import tech.wetech.flexmodel.codegen.GenerationContext;
import tech.wetech.flexmodel.codegen.GenerationTool;
import tech.wetech.flexmodel.codegen.ModelClass;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.codegen.enumeration.ApiType;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDefinitionApplicationService {

  @Inject
  ApiDefinitionService apiDefinitionService;

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

  public List<ApiDefinitionTreeDTO> findApiDefinitionTree() {
    List<ApiDefinition> list = apiDefinitionService.findList();
    List<ApiDefinitionTreeDTO> root = list.stream()
      .filter(apiDefinition -> apiDefinition.getParentId() == null)
      .map(ApiDefinitionTreeDTO::new).toList();
    for (ApiDefinitionTreeDTO treeDTO : root) {
      treeDTO.setChildren(getChildren(treeDTO, list));
    }
    return root;
  }

  private List<ApiDefinitionTreeDTO> getChildren(ApiDefinitionTreeDTO treeDTO, List<ApiDefinition> list) {
    List<ApiDefinitionTreeDTO> result = new ArrayList<>();
    for (ApiDefinition apiDefinition : list) {
      if (treeDTO.getId().equals(apiDefinition.getParentId())) {
        ApiDefinitionTreeDTO dto = new ApiDefinitionTreeDTO(apiDefinition);
        dto.setChildren(getChildren(dto, list));
        result.add(dto);
      }
    }
    return result;
  }

  public ApiDefinition createApiDefinition(ApiDefinition apiDefinition) {
    return apiDefinitionService.create(apiDefinition);
  }

  public ApiDefinition updateApiDefinition(ApiDefinition apiDefinition) {
    return apiDefinitionService.update(apiDefinition);
  }

  public ApiDefinition updateApiDefinitionIgnoreNull(ApiDefinition apiDefinition) {
    return apiDefinitionService.updateIgnoreNull(apiDefinition);
  }

  public void deleteApiDefinition(String id) {
    apiDefinitionService.delete(id);
  }

  public void generateAPIs(GenerateAPIsDTO dto) {
    List<String> generateAPIs = dto.getGenerateAPIs();
    try (Session session = sessionFactory.createSession(dto.getDatasourceName())) {
      Entity entity = (Entity) session.getModel(dto.getModelName());
      ApiDefinition apiFolder = createApiFolder(dto);
      for (String type : generateAPIs) {
        ApiDefinitionGenerator apiDefinitionGenerator = templateMap.get(type);
        if (apiDefinitionGenerator != null) {
          ModelClass modelClass = GenerationTool.buildModelClass(null, dto.getDatasourceName(), entity);
          GenerationContext generationContext = new GenerationContext();
          generationContext.setModelClass(modelClass);
          generationContext.putVariable("idFieldOfPath", dto.getIdFieldOfPath());
          generationContext.putVariable("apiParentId", apiFolder.getId());
          ApiDefinition apiDefinition = apiDefinitionGenerator.createApiDefinition(generationContext);
          apiDefinitionService.create(apiDefinition);
        }
      }
    }
  }

  private ApiDefinition createApiFolder(GenerateAPIsDTO dto) {
    ApiDefinition folder = new ApiDefinition();
    folder.setParentId(null);
    folder.setMethod(null);
    folder.setPath(null);
    folder.setName(dto.getApiFolder());
    folder.setEnabled(false);
    folder.setType(ApiType.FOLDER);
    return apiDefinitionService.create(folder);
  }

}
