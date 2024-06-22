package tech.wetech.flexmodel.application;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.domain.model.api.ApiInfo;
import tech.wetech.flexmodel.domain.model.api.ApiInfoService;
import tech.wetech.flexmodel.domain.model.api.ApiLog;
import tech.wetech.flexmodel.domain.model.api.ApiLogService;
import tech.wetech.flexmodel.domain.model.data.DataService;
import tech.wetech.flexmodel.domain.model.idp.IdentityProviderService;
import tech.wetech.flexmodel.util.UriTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author cjbi
 */
@ApplicationScoped
@Slf4j
public class ApiRuntimeApplicationService {

  @Inject
  ApiInfoService apiInfoService;

  @Inject
  IdentityProviderService identityProviderService;

  @Inject
  ApiLogService apiLogService;

  @Inject
  DataService dataService;

  public List<ApiLog> findApiLogs(String filter, int current, int pageSize) {
    return apiLogService.find(filter, current, pageSize);
  }

  @SuppressWarnings("all")
  public void accept(RoutingContext routingContext) {
    log(routingContext, () -> {
      List<ApiInfo> apis = apiInfoService.findList();
      for (ApiInfo apiInfo : apis) {
        UriTemplate uriTemplate = new UriTemplate("/api/v1" + apiInfo.getPath());
        Map<String, String> pathParameters = uriTemplate.match(new UriTemplate(routingContext.normalizedPath()));
        String method = routingContext.request().method().name();
        if (pathParameters != null && method.equals(apiInfo.getMethod())) {
          log.debug("Matched request for api: {}", apiInfo);
          boolean isAuth = (boolean) apiInfo.getMeta().get("auth");
          if (isAuth) {
            String identityProvider = (String) apiInfo.getMeta().get("identityProvider");
            String authorization = Objects.toString(routingContext.request().getHeader("Authorization"), "");
            String token = authorization.replace("Bearer", "").trim();
            boolean active = identityProviderService.checkToken(identityProvider, token);
            if (!active) {
              Map<String, Object> result = new HashMap<>();
              result.put("messasge", "Authentication failed.");
              result.put("code", -1);
              result.put("success", false);
              routingContext.response()
                .setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
                .putHeader("Content-Type", "application/json")
                .end(JsonUtils.getInstance().stringify(result));
              return;
            }
          }
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
    });

  }

  public void log(RoutingContext routingContext, Runnable runnable) {
    ApiLog apiLog = new ApiLog();
    ApiLog.Data apiData = new ApiLog.Data();
    apiLog.setData(apiData);
    long beginTime = System.currentTimeMillis();
    try {
      apiData.setMethod(routingContext.request().method().name());
      apiData.setPath(routingContext.request().path());
      apiData.setReferer(routingContext.request().getHeader("Referer"));
      apiData.setRemoteIp(routingContext.request().remoteAddress().host());
      apiData.setUserAgent(routingContext.request().getHeader("User-Agent"));
      runnable.run();
    } catch (Exception e) {
      routingContext.response()
        .setStatusCode(500);
      apiLog.setLevel(ApiLog.Level.ERROR);
      apiData.setStatus(500);
      apiData.setErrors(e.getMessage());
      throw e;
    } finally {
      apiLog.setUri(apiData.getMethod() + " " + apiData.getPath());
      apiLog.setCreatedAt(LocalDateTime.now());
      apiData.setStatus(routingContext.response().getStatusCode());
      apiData.setMessage(routingContext.response().getStatusMessage());
      apiData.setExecTime(System.currentTimeMillis() - beginTime);
      apiLogService.create(apiLog);
    }

  }

}
