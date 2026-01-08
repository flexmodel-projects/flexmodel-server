package tech.wetech.flexmodel.application;

import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.application.dto.ApiDefinitionTreeDTO;
import tech.wetech.flexmodel.application.dto.GenerateAPIsDTO;
import tech.wetech.flexmodel.application.dto.GraphQLRefreshEvent;
import tech.wetech.flexmodel.codegen.GenerationContext;
import tech.wetech.flexmodel.codegen.ModelClass;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.codegen.entity.ApiDefinitionHistory;
import tech.wetech.flexmodel.codegen.enumeration.ApiType;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionService;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.shared.SessionContextHolder;

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

  public List<ApiDefinitionTreeDTO> findApiDefinitionTree() {
    List<ApiDefinition> list = apiDefinitionService.findList(SessionContextHolder.getProjectId());
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
   * @param appDefinitionId
   * @return
   */
  public List<ApiDefinitionHistory> findApiDefinitionHistories(String appDefinitionId) {
    return apiDefinitionService.findApiDefinitionHistories(appDefinitionId);
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

  public void deleteApiDefinition(String id) {
    apiDefinitionService.delete(id);
  }

  @Transactional
  public void generateAPIs(GenerateAPIsDTO dto) {
    List<String> generateAPIs = dto.getGenerateAPIs();
    try (Session session = sessionFactory.createSession(dto.getDatasourceName())) {
      EntityDefinition entity = (EntityDefinition) session.schema().getModel(dto.getModelName());
      ApiDefinition apiFolder = createApiFolder(dto);
      for (String type : generateAPIs) {
        ApiDefinitionGenerator apiDefinitionGenerator = templateMap.get(type);
        if (apiDefinitionGenerator != null) {
          ModelClass modelClass = ModelClass.buildModelClass(null, dto.getDatasourceName(), entity);
          GenerationContext generationContext = new GenerationContext();
          generationContext.setModelClass(modelClass);
          generationContext.putVariable("idFieldOfPath", dto.getIdFieldOfPath());
          generationContext.putVariable("apiParentId", apiFolder.getId());
          ApiDefinition apiDefinition = apiDefinitionGenerator.createApiDefinition(generationContext);
          apiDefinitionService.create(apiDefinition);
        }
      }
    }
    eventBus.publish("graphql.refresh", new GraphQLRefreshEvent());
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

  public ApiDefinition findApiDefinition(String id) {
    return apiDefinitionService.findApiDefinition(id);
  }

  public ApiDefinitionHistory restoreApiDefinition(String historyId) {
    ApiDefinitionHistory apiDefinitionHistory = apiDefinitionService.findApiDefinitionHistory(historyId);
    if (apiDefinitionHistory != null) {
      ApiDefinition apiDefinition = JsonUtils.convertValue(apiDefinitionHistory, ApiDefinition.class);
      apiDefinition.setId(apiDefinitionHistory.getApiDefinitionId());
      apiDefinitionService.update(apiDefinition);
    }
    return apiDefinitionHistory;
  }
}
