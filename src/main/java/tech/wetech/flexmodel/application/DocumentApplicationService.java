package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.api.ApiInfo;
import tech.wetech.flexmodel.domain.model.api.ApiInfoService;

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

  public Map<String, Object> getOpenApi() {
    Map<String, Object> openAPI = new HashMap<>();
    openAPI.put("openapi", "3.0.3");
    openAPI.put("info", Map.of(
      "title", "Flexmodel API document",
      "description", """
        |-
          This is a sample Pet Store Server based on the OpenAPI 3.0 specification.  You can find out more about
          Swagger at [https://swagger.io](https://swagger.io). In the third iteration of the pet store, we've switched to the design first approach!
          You can now help us improve the API whether it's by making changes to the definition itself or to the code.
          That way, with time, we can improve the API in general, and expose some of the new features in OAS3.

          _If you're looking for the Swagger 2.0/OAS 2.0 version of Petstore, then click [here](https://editor.swagger.io/?url=https://petstore.swagger.io/v2/swagger.yaml). Alternatively, you can load via the `Edit > Load Petstore OAS 2.0` menu option!_
          Some useful links:
          - [The Pet Store repository](https://github.com/swagger-api/swagger-petstore)
          - [The source API definition for the Pet Store](https://github.com/swagger-api/swagger-petstore/blob/master/src/main/resources/openapi.yaml)
        """
    ));
//    openAPI.put("host", "127.0.0.1");
    openAPI.put("components", buildComponents());

    // add bearer token

    openAPI.put("servers", List.of(Map.of("url", "/api/v1")));
    openAPI.put("schemas", List.of("https", "http"));

    List<ApiInfo> apis = apiInfoService.findList();
    openAPI.put("tags", buildTags(apis));
    openAPI.put("paths", buildPaths(apis));
    return openAPI;
  }

  public Map<String, Object> buildComponents() {
    return Map.of("securitySchemes", Map.of(
      "bearerAuth", Map.of(
        "type", "apiKey",
        "name", "Authorization",
        "in", "header"
      )
    ));
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
          case "list" -> {
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
            yield List.of(current, pageSize);
          }
          case "view" -> {
            Map<String, Object> id = new HashMap<>();
            id.put("name", "id");
            id.put("in", "path");
            id.put("description", "ID of view to return");
            id.put("required", true);
//            id.put("type", "integer");
//            id.put("format", "int64");
            yield List.of(id);
          }
          case "create" -> {
            Map<String, Object> name = new HashMap<>();
            name.put("name", "id");
            name.put("in", "body");
            name.put("description", "ID of view to return");
            name.put("required", true);
            name.put("type", "string");
            yield List.of(name);
          }
          case "update" -> {
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
            yield List.of(id, name);
          }
          case "delete" -> {
            Map<String, Object> id = new HashMap<>();
            id.put("name", "id");
            id.put("in", "path");
            id.put("description", "ID of view to return");
            id.put("required", true);
            id.put("type", "integer");
            id.put("format", "int64");
            yield List.of(id);
          }
          default -> throw new IllegalStateException("Unexpected value: " + restAPIType);
        });
        Map<String, Object> responses = new HashMap<>();
        responses.put("400", Map.of("description", "Invalid username supplied"));
        responses.put("404", Map.of("description", "User not found"));
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

}
