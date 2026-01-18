package dev.flexmodel.application;

import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import dev.flexmodel.*;
import dev.flexmodel.application.dto.ApiDefinitionTreeDTO;
import dev.flexmodel.application.dto.GenerateAPIsDTO;
import dev.flexmodel.application.dto.GraphQLRefreshEvent;
import dev.flexmodel.codegen.GenerationContext;
import dev.flexmodel.codegen.ModelClass;
import dev.flexmodel.codegen.entity.ApiDefinition;
import dev.flexmodel.codegen.entity.ApiDefinitionHistory;
import dev.flexmodel.codegen.enumeration.ApiType;
import dev.flexmodel.domain.model.api.ApiDefinitionService;
import dev.flexmodel.model.EntityDefinition;
import dev.flexmodel.session.Session;
import dev.flexmodel.session.SessionFactory;

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

  @Inject
  EventBus eventBus;

  private static final Map<String, ApiDefinitionGenerator> templateMap = new HashMap<>();

  static {
    templateMap.put("list", new ListApiDefinitionGenerator());
    templateMap.put("pagination", new PaginationApiDefinitionGenerator());
    templateMap.put("view", new ViewApiDefinitionGenerator());
    templateMap.put("create", new CreateApiDefinitionGenerator());
    templateMap.put("update", new UpdateApiDefinitionGenerator());
    templateMap.put("delete", new DeleteApiDefinitionGenerator());

  }

  public List<ApiDefinitionTreeDTO> findApiDefinitionTree(String projectId) {
    List<ApiDefinition> list = apiDefinitionService.findList(projectId);
    List<ApiDefinitionTreeDTO> root = list.stream()
      .filter(apiDefinition -> apiDefinition.getParentId() == null)
      .map(ApiDefinitionTreeDTO::new).toList();
    for (ApiDefinitionTreeDTO treeDTO : root) {
      treeDTO.setChildren(getChildren(treeDTO, list));
    }
    return root;
  }

  /**
   * 查询API定义历史
   *
   * @param projectId
   * @param apiDefinitionId
   * @return
   */
  public List<ApiDefinitionHistory> findApiDefinitionHistories(String projectId, String apiDefinitionId) {
    return apiDefinitionService.findApiDefinitionHistories(projectId, apiDefinitionId);
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

  public ApiDefinition createApiDefinition(String projectId, ApiDefinition apiDefinition) {
    return apiDefinitionService.create(apiDefinition);
  }

  public ApiDefinition updateApiDefinition(String projectId, ApiDefinition apiDefinition) {
    return apiDefinitionService.update(apiDefinition);
  }

  public void deleteApiDefinition(String projectId, String id) {
    apiDefinitionService.delete(projectId, id);
  }

  @Transactional
  public void generateAPIs(String projectId, GenerateAPIsDTO dto) {
    List<String> generateAPIs = dto.getGenerateAPIs();
    try (Session session = sessionFactory.createSession(dto.getDatasourceName())) {
      EntityDefinition entity = (EntityDefinition) session.schema().getModel(dto.getModelName());
      ApiDefinition apiFolder = createApiFolder(projectId, dto);
      for (String type : generateAPIs) {
        ApiDefinitionGenerator apiDefinitionGenerator = templateMap.get(type);
        if (apiDefinitionGenerator != null) {
          ModelClass modelClass = ModelClass.buildModelClass(null, dto.getDatasourceName(), entity);
          GenerationContext generationContext = new GenerationContext();
          generationContext.setModelClass(modelClass);
          generationContext.putVariable("idFieldOfPath", dto.getIdFieldOfPath());
          generationContext.putVariable("apiParentId", apiFolder.getId());
          ApiDefinition apiDefinition = apiDefinitionGenerator.createApiDefinition(projectId, generationContext);
          apiDefinition.setProjectId(projectId);
          apiDefinitionService.create(apiDefinition);
        }
      }
    }
    eventBus.publish("graphql.refresh", new GraphQLRefreshEvent());
  }

  private ApiDefinition createApiFolder(String projectId, GenerateAPIsDTO dto) {
    ApiDefinition folder = new ApiDefinition();
    folder.setParentId(null);
    folder.setMethod(null);
    folder.setPath(null);
    folder.setName(dto.getApiFolder());
    folder.setEnabled(false);
    folder.setType(ApiType.FOLDER);
    folder.setProjectId(projectId);
    return apiDefinitionService.create(folder);
  }

  public ApiDefinition findApiDefinition(String projectId, String id) {
    return apiDefinitionService.findApiDefinition(projectId, id);
  }

  public ApiDefinitionHistory restoreApiDefinition(String projectId, String historyId) {
    ApiDefinitionHistory apiDefinitionHistory = apiDefinitionService.findApiDefinitionHistory(projectId, historyId);
    if (apiDefinitionHistory != null) {
      ApiDefinition apiDefinition = JsonUtils.convertValue(apiDefinitionHistory, ApiDefinition.class);
      apiDefinition.setId(apiDefinitionHistory.getApiDefinitionId());
      apiDefinition.setProjectId(projectId);
      apiDefinitionService.update(apiDefinition);
    }
    return apiDefinitionHistory;
  }
}
