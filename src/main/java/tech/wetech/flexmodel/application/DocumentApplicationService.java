package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.TypedField;
import tech.wetech.flexmodel.domain.model.api.ApiInfo;
import tech.wetech.flexmodel.domain.model.api.ApiInfoService;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.domain.model.api.ApiInfo.Type.FOLDER;
import static tech.wetech.flexmodel.domain.model.api.ApiInfo.Type.REST_API;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DocumentApplicationService {

  @Inject
  ApiInfoService apiInfoService;

  @Inject
  ModelService modelService;

  public Map<String, Object> getOpenApi() {
    List<ApiInfo> apis = apiInfoService.findList();
    Map<String, Object> openAPI = new HashMap<>();
    openAPI.put("openapi", "3.0.3");
    openAPI.put("info", buildInfo());
    openAPI.put("components", buildComponents(apis));
    openAPI.put("servers", List.of(Map.of("url", "/api/v1")));
    openAPI.put("schemas", List.of("https", "http"));
    openAPI.put("tags", buildTags(apis));
    openAPI.put("paths", buildPaths(apis));
    return openAPI;
  }

  private Map<String, String> buildInfo() {
    return Map.of(
      "title", "Flexmodel API document",
      "description", """
        Learn how to interact with Flexmodel programmatically
        """
    );
  }

  private Map<String, Object> buildSchemas(List<ApiInfo> apis) {
    Map<String, Object> typeMapping = new HashMap<>();
    typeMapping.put("string", Map.of("type", "string"));
    typeMapping.put("text", Map.of("type", "string"));
    typeMapping.put("int", Map.of("type", "integer", "format", "int32"));
    typeMapping.put("long", Map.of("type", "integer", "format", "int64"));
    typeMapping.put("decimal", Map.of("type", "number", "format", "double"));
    typeMapping.put("boolean", Map.of("type", "boolean"));
//    typeMapping.put("", Map.of("type", "array"));
    typeMapping.put("json", Map.of("type", "object"));
//    typeMapping.put("", Map.of("type", "null"));
    Map<String, Object> definitions = new HashMap<>();
    for (ApiInfo api : apis) {
      if (api.getType() != REST_API) {
        continue;
      }
      String datasourceName = (String) api.getMeta().get("datasource");
      String modelName = (String) api.getMeta().get("model");
      Entity entity = modelService.findModel(datasourceName, modelName).orElseThrow();
      if (entity == null) {
        continue;
      }
      Map<String, Object> object = new HashMap<>();
      definitions.put(datasourceName + "_" + entity.getName(), object);
      object.put("type", "object");
      Map<String, Object> properties = new HashMap<>();
      object.put("properties", properties);
      for (TypedField<?, ?> field : entity.getFields()) {
        properties.put(field.getName(), typeMapping.getOrDefault(field.getType(), Map.of("type", "string")));
      }
    }
    return definitions;
  }

  public Map<String, Object> buildComponents(List<ApiInfo> apis) {
    Map<String, Object> components = new HashMap<>();
    components.put("securitySchemes", Map.of(
      "bearerAuth", Map.of(
        "type", "apiKey",
        "name", "Authorization",
        "in", "header"
      )
    ));
    components.put("schemas", buildSchemas(apis));
    return components;
  }

  public List<Map<String, Object>> buildTags(List<ApiInfo> apis) {
    List<Map<String, Object>> tags = new ArrayList<>();
    for (ApiInfo api : apis) {
      if (api.getType() == FOLDER) {
        Map<String, Object> tag = new HashMap<>();
        tag.put("name", api.getName());
        tag.put("description", api.getName());
        tags.add(tag);
      }
    }
    return tags;
  }

  public Map<String, Object> buildPaths(List<ApiInfo> apis) {
    Map<String, Object> paths = new HashMap<>();
    for (ApiInfo api : apis) {
      if (api.getType() == REST_API) {
        Map<String, Object> path = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        content.put("tags", List.of(
          apis.stream()
            .filter(a -> a.getId().equals(api.getParentId()))
            .map(ApiInfo::getName)
            .findFirst()
            .orElseThrow()
        ));
        String restAPIType = (String) api.getMeta().get("type");
        content.put("summary", api.getName());
        content.put("operationId", restAPIType);
        content.put("produces", List.of("application/json"));
        content.put("parameters", switch (restAPIType) {
          case "list" -> getListApiParameter(api);
          case "view" -> getViewApiParameter(api);
          case "create" -> getCreateApiParameter(api);
          case "update" -> getUpdateApiParameter(api);
          case "delete" -> getDeleteApiParameter(api);
          default -> throw new IllegalStateException("Unexpected value: " + restAPIType);
        });
        Map<String, Object> responses = new HashMap<>();
        responses.put("200", switch (restAPIType) {
          case "list" -> getListApiResponse200(api);
          case "view" -> getViewApiResponse200(api);
          case "create" -> getCreateApiResponse200(api);
          case "update" -> getUpdateApiResponse200(api);
          case "delete" -> getDeleteApiResponse200(api);
          default -> throw new IllegalStateException("Unexpected value: " + restAPIType);
        });
        responses.put("400", Map.of("description", "invalid input"));
        responses.put("404", Map.of("description", "not found"));
        content.put("responses", responses);
        boolean isAuth = (boolean) api.getMeta().get("auth");
        if (isAuth) {
          content.put("security", List.of(Map.of("bearerAuth", List.of())));
        }

        path.put(api.getMethod().toLowerCase(), content);
        if (paths.containsKey(api.getPath())) {
          Map<String, Object> existPath = (Map<String, Object>) paths.get(api.getPath());
          existPath.put(api.getMethod().toLowerCase(), content);
        } else {
          paths.put(api.getPath(), path);
        }
      }
    }
    return paths;
  }

  private List<Map<String, Object>> getDeleteApiParameter(ApiInfo api) {
    Map<String, Object> id = new HashMap<>();
    id.put("name", "id");
    id.put("in", "path");
    id.put("description", "ID of view to return");
    id.put("required", true);
    id.put("type", "integer");
    id.put("format", "int64");
    return List.of(id);
  }

  private List<Map<String, Object>> getUpdateApiParameter(ApiInfo api) {
    Map<String, Object> id = new HashMap<>();
    id.put("name", "id");
    id.put("in", "path");
    id.put("description", "ID of view to return");
    id.put("required", true);
    id.put("type", "integer");
    id.put("format", "int64");
    Map<String, Object> name = new HashMap<>();
    name.put("name", "id");
    name.put("in", "body");
    name.put("description", "ID of view to return");
    name.put("required", true);
    name.put("type", "string");
    return List.of(id, name);
  }

  private List<Map<String, Object>> getCreateApiParameter(ApiInfo api) {
    Map<String, Object> name = new HashMap<>();
    name.put("name", "id");
    name.put("in", "body");
    name.put("description", "ID of view to return");
    name.put("required", true);
    name.put("type", "string");
    return List.of(name);
  }

  private List<Map<String, Object>> getViewApiParameter(ApiInfo api) {
    Map<String, Object> id = new HashMap<>();
    id.put("name", "id");
    id.put("in", "path");
    id.put("description", "ID of view to return");
    id.put("required", true);
//            id.put("type", "integer");
//            id.put("format", "int64");
    return List.of(id);
  }

  private List<Map<String, Object>> getListApiParameter(ApiInfo api) {
    Map<String, Object> pageSize = new HashMap<>();
    pageSize.put("name", "pageSize");
    pageSize.put("in", "query");
    pageSize.put("description", "Specify the max returned records per page (default to 30).");
    pageSize.put("required", false);
    pageSize.put("type", "integer");
    pageSize.put("format", "int64");
    Map<String, Object> current = new HashMap<>();
    current.put("name", "current");
    current.put("in", "query");
    current.put("description", "The page (aka. offset) of the paginated list (default to 1).");
    current.put("required", false);
    current.put("type", "integer");
    current.put("format", "int64");
    return List.of(current, pageSize);
  }

  private Map<String, Object> getListApiResponse200(ApiInfo api) {
    String datasourceName = (String) api.getMeta().get("datasource");
    String modelName = (String) api.getMeta().get("model");
    boolean paging = (Boolean) api.getMeta().get("paging");
    Entity entity = modelService.findModel(datasourceName, modelName).orElseThrow();
    var resProps = new HashMap<>();
    resProps.put("list", Map.of(
      "type", "array",
      "items", Map.of("$ref", "#/components/schemas/" + datasourceName + "_" + entity.getName())
    ));
    if (paging) {
      resProps.put("total", Map.of("type", "integer", "format", "int64"));
    }
    return Map.of(
      "description", "successful operation",
      "content", Map.of(
        "application/json",
        Map.of("schema",
          Map.of(
            "type", "object",
            "properties", resProps
          )
        )
      )
    );
  }

  private Map<String, Object> getViewApiResponse200(ApiInfo api) {
    String datasourceName = (String) api.getMeta().get("datasource");
    String modelName = (String) api.getMeta().get("model");
    Entity entity = modelService.findModel(datasourceName, modelName).orElseThrow();
    return Map.of(
      "description", "successful operation",
      "content", Map.of(
        "application/json",
        Map.of("schema",
          Map.of("$ref", "#/components/schemas/" + datasourceName + "_" + entity.getName())))
    );
  }

  private Map<String, Object> getCreateApiResponse200(ApiInfo api) {
    String datasourceName = (String) api.getMeta().get("datasource");
    String modelName = (String) api.getMeta().get("model");
    Entity entity = modelService.findModel(datasourceName, modelName).orElseThrow();
    return Map.of(
      "description", "successful operation",
      "content", Map.of(
        "application/json",
        Map.of("schema",
          Map.of("$ref", "#/components/schemas/" + datasourceName + "_" + entity.getName())))
    );
  }

  private Map<String, Object> getUpdateApiResponse200(ApiInfo api) {
    String datasourceName = (String) api.getMeta().get("datasource");
    String modelName = (String) api.getMeta().get("model");
    Entity entity = modelService.findModel(datasourceName, modelName).orElseThrow();
    return Map.of(
      "description", "successful operation",
      "content", Map.of(
        "application/json",
        Map.of("schema",
          Map.of("$ref", "#/components/schemas/" + datasourceName + "_" + entity.getName())))
    );
  }

  private Map<String, Object> getDeleteApiResponse200(ApiInfo api) {
    return Map.of();
  }


}
