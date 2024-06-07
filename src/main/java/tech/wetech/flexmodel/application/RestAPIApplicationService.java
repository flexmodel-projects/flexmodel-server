package tech.wetech.flexmodel.application;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.domain.model.apidesign.ApiInfo;
import tech.wetech.flexmodel.domain.model.apidesign.ApiInfoService;
import tech.wetech.flexmodel.domain.model.data.DataService;
import tech.wetech.flexmodel.util.UriTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
@Slf4j
public class RestAPIApplicationService {

  @Inject
  ApiInfoService apiInfoService;

  @Inject
  DataService dataService;

  @SuppressWarnings("all")
  public void apply(RoutingContext routingContext) {
    List<ApiInfo> apis = apiInfoService.findList();
    for (ApiInfo apiInfo : apis) {
      UriTemplate uriTemplate = new UriTemplate("/api/v1" + apiInfo.getPath());
      Map<String, String> pathParameters = uriTemplate.match(new UriTemplate(routingContext.normalizedPath()));
      String method = routingContext.request().method().name();
      if (pathParameters != null && method.equals(apiInfo.getMethod())) {
        log.debug("Matched request for api: {}", apiInfo);
        String restAPIType = (String) apiInfo.getMeta().get("type");
        String datasourceName = (String) apiInfo.getMeta().get("datasource");
        Map<String, Object> model = (Map<String, Object>) apiInfo.getMeta().get("model");
        String modelName = (String) model.get("name");
        switch (restAPIType) {
          case "list" -> {
            boolean paging = (Boolean) apiInfo.getMeta().get("paging");
            String filter = routingContext.request().getParam("filter");
            String sort = routingContext.request().getParam("sort");
            Map<String, Object> result = new HashMap<>();
            if (paging) {
              int current = Integer.parseInt(routingContext.request().getParam("current", "1"));
              int pageSize = Integer.parseInt(routingContext.request().getParam("pageSize", "30"));
              List<Map<String, Object>> list = dataService.findRecords(datasourceName, modelName,
                current, pageSize, filter, sort);
              long total = dataService.countRecords(datasourceName, modelName, filter);
              result.put("total", total);
              result.put("list", list);
            } else {
              List<Map<String, Object>> list = dataService.findRecords(datasourceName, modelName,
                null, null, filter, sort);
              result.put("list", list);
            }
            routingContext.response()
              .putHeader("Content-Type", "application/json")
              .end(JsonUtils.getInstance().stringify(result));
          }
          case "view" -> {
            Map<String, Object> idField = (Map<String, Object>) model.get("idField");
            String id = pathParameters.get(idField.get("name"));
            if (id == null) {
              id = routingContext.request().getParam((String) idField.get("name"));
            }
            if (id == null) {
              throw new IllegalArgumentException("Id nust not be null");
            }
            Map<String, Object> record = dataService.findOneRecord(datasourceName, modelName, id);
            routingContext.response()
              .putHeader("Content-Type", "application/json")
              .end(JsonUtils.getInstance().stringify(record));
          }
          case "create" -> {
            String body = routingContext.request()
              .body()
              .result()
              .toString();
            Map<String, Object> data = JsonUtils.getInstance().parseToObject(body, Map.class);
            dataService.createRecord(datasourceName, modelName, data);
          }
          case "update" -> {
            Map<String, Object> idField = (Map<String, Object>) model.get("idField");
            String id = pathParameters.get(idField.get("name"));
            if (id == null) {
              id = routingContext.request().getParam((String) idField.get("name"));
            }
            String body = routingContext.request()
              .body()
              .result()
              .toString();
            Map<String, Object> data = JsonUtils.getInstance().parseToObject(body, Map.class);
            if (id == null) {
              id = (String) data.get(idField.get("name"));
            }
            if (id == null) {
              throw new IllegalArgumentException("Id nust not be null");
            }
            dataService.updateRecord(datasourceName, modelName, id, data);
          }
          case "delete" -> {
            Map<String, Object> idField = (Map<String, Object>) model.get("idField");
            String id = pathParameters.get(idField.get("name"));
            if (id == null) {
              id = routingContext.request().getParam((String) idField.get("name"));
            }
            if (id == null) {
              throw new IllegalArgumentException("Id nust not be null");
            }
            dataService.deleteRecord(datasourceName, modelName, id);
          }
          default -> {
            routingContext.response().end("Matched request for path: " + routingContext.normalisedPath());
          }
        }
      }
    }
  }

}
