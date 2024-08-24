package tech.wetech.flexmodel.application;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.IDField;
import tech.wetech.flexmodel.codegen.entity.ApiInfo;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.domain.model.api.*;
import tech.wetech.flexmodel.domain.model.data.DataService;
import tech.wetech.flexmodel.domain.model.idp.IdentityProviderService;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;
import tech.wetech.flexmodel.graphql.GraphQLProvider;
import tech.wetech.flexmodel.util.JsonUtils;
import tech.wetech.flexmodel.util.UriTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static graphql.ExecutionInput.newExecutionInput;

/**
 * @author cjbi
 */
@ApplicationScoped
@Slf4j
@SuppressWarnings("all")
public class ApiRuntimeApplicationService {

  @Inject
  ApiInfoService apiInfoService;

  @Inject
  IdentityProviderService identityProviderService;

  @Inject
  ApiLogService apiLogService;

  @Inject
  ModelService modelService;

  @Inject
  DataService dataService;

  @Inject
  GraphQLProvider graphQLProvider;

  public ExecutionResult execute(String operationName, String query, Map<String, Object> variables) {
    GraphQL graphQL = graphQLProvider.getGraphQL();
    if (variables == null) {
      variables = new HashMap<>();
    }
    ExecutionInput executionInput = newExecutionInput()
      .operationName(operationName)
      .query(query)
      .variables(variables)
      .build();
    return graphQL.execute(executionInput);
  }


  public List<ApiLog> findApiLogs(String filter, int current, int pageSize) {
    return apiLogService.find(filter, current, pageSize);
  }

  public List<LogStat> stat(String filter) {
    return apiLogService.stat(filter);
  }

  @SuppressWarnings("all")
  public void accept(RoutingContext routingContext) {
    log(routingContext, () -> {
      List<ApiInfo> apis = apiInfoService.findList();
      for (ApiInfo apiInfo : apis) {
        Map<String, Object> meta = (Map<String, Object>) apiInfo.getMeta();
        UriTemplate uriTemplate = new UriTemplate("/api/v1" + apiInfo.getPath());
        Map<String, String> pathParameters = uriTemplate.match(new UriTemplate(routingContext.normalizedPath()));
        String method = routingContext.request().method().name();
        if (pathParameters != null && method.equals(apiInfo.getMethod())) {
          log.debug("Matched request for api: {}", apiInfo);
          boolean isAuth = (boolean) meta.get("auth");
          if (isAuth) {
            String identityProvider = (String) meta.get("identityProvider");
            String authorization = Objects.toString(routingContext.request().getHeader("Authorization"), "");
            String token = authorization.replace("Bearer", "").trim();
            boolean active = identityProviderService.checkToken(identityProvider, token);
            if (!active) {
              sendAuthField(routingContext);
              return;
            }
          }
          Map execution = (Map) meta.get("execution");
          String operationName = (String) execution.get("operationName");
          String query = (String) execution.get("query");
          String restAPIType = (String) meta.get("type");
          Map<String, Object> defaultVariables = (Map<String, Object>) execution.get("variables");
          Map<String, Object> variables = new HashMap<>();
          if (defaultVariables != null) {
            variables.putAll(defaultVariables);
          }
          if (method.equals("GET")) {
            ExecutionResult result = execute(operationName, query, defaultVariables);
            routingContext.response()
              .putHeader("Content-Type", "application/json")
              .end(JsonUtils.getInstance().stringify(result));
          } else {
            String bodyString = routingContext.body().asString();
            Map body = (Map) JsonUtils.getInstance().parseToObject(bodyString, Map.class);
            // 请求体
            if (body != null) {
              variables.putAll(body);
            }
            // 路径参数
            variables.putAll(pathParameters);
            ExecutionResult result = execute(operationName, query, variables);
            routingContext.response()
              .putHeader("Content-Type", "application/json")
              .end(JsonUtils.getInstance().stringify(result));
          }
//          switch (restAPIType) {
//            case "list" -> doList(routingContext, pathParameters, apiInfo);
//            case "view" -> doView(routingContext, pathParameters, apiInfo);
//            case "create" -> doCreate(routingContext, pathParameters, apiInfo);
//            case "update" -> doUpdate(routingContext, pathParameters, apiInfo);
//            case "delete" -> doDelete(routingContext, pathParameters, apiInfo);
//            default -> {
//              routingContext.response().end("Matched request for path: " + routingContext.normalisedPath());
//            }
//          }
          break;
        }
      }
    });
  }

  private void sendAuthField(RoutingContext routingContext) {
    Map<String, Object> result = new HashMap<>();
    result.put("messasge", "Authentication failed.");
    result.put("code", -1);
    result.put("success", false);
    routingContext.response()
      .setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
      .putHeader("Content-Type", "application/json")
      .end(JsonUtils.getInstance().stringify(result));
  }

  private void doDelete(RoutingContext routingContext, Map<String, String> pathParameters, ApiInfo apiInfo) {
    Map<String, Object> meta = (Map<String, Object>) apiInfo.getMeta();
    String datasourceName = (String) meta.get("datasource");
    String modelName = (String) meta.get("model");
    Entity model = modelService.findModel(datasourceName, modelName).orElseThrow();
    IDField idField = model.findIdField().orElseThrow();
    String id = pathParameters.get(idField.getName());
    if (id == null) {
      id = routingContext.request().getParam(idField.getName());
    }
    if (id == null) {
      throw new IllegalArgumentException("Id nust not be null");
    }
    dataService.deleteRecord(datasourceName, modelName, id);
    routingContext.response()
      .putHeader("Content-Type", "application/json")
      .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
      .end();

  }

  private void doUpdate(RoutingContext routingContext, Map<String, String> pathParameters, ApiInfo apiInfo) {
    Map<String, Object> meta = (Map<String, Object>) apiInfo.getMeta();
    String datasourceName = (String) meta.get("datasource");
    String modelName = (String) meta.get("model");
    Entity model = modelService.findModel(datasourceName, modelName).orElseThrow();
    IDField idField = model.findIdField().orElseThrow();
    String id = pathParameters.get(idField.getName());
    if (id == null) {
      id = routingContext.request().getParam((String) idField.getName());
    }
    String body = routingContext.body().asString();
    Map<String, Object> data = JsonUtils.getInstance().parseToObject(body, Map.class);
    if (id == null) {
      id = (String) data.get(idField.getName());
    }
    if (id == null) {
      throw new IllegalArgumentException("Id nust not be null");
    }
    Map<String, Object> result = dataService.updateRecord(datasourceName, modelName, id, data);
    routingContext.response()
      .putHeader("Content-Type", "application/json")
      .end(JsonUtils.getInstance().stringify(result));
  }

  private void doCreate(RoutingContext routingContext, Map<String, String> pathParameters, ApiInfo apiInfo) {
    Map<String, Object> meta = (Map<String, Object>) apiInfo.getMeta();
    String datasourceName = (String) meta.get("datasource");
    String modelName = (String) meta.get("model");
    Entity model = modelService.findModel(datasourceName, modelName).orElseThrow();
    IDField idField = model.findIdField().orElseThrow();
    String body = routingContext.body().asString();
    Map<String, Object> data = JsonUtils.getInstance().parseToObject(body, Map.class);
    Map<String, Object> result = dataService.createRecord(datasourceName, modelName, data);
    routingContext.response()
      .putHeader("Content-Type", "application/json")
      .end(JsonUtils.getInstance().stringify(result));
  }

  private void doView(RoutingContext routingContext, Map<String, String> pathParameters, ApiInfo apiInfo) {
    Map<String, Object> meta = (Map<String, Object>) apiInfo.getMeta();
    String datasourceName = (String) meta.get("datasource");
    String modelName = (String) meta.get("model");
    Entity model = modelService.findModel(datasourceName, modelName).orElseThrow();
    IDField idField = model.findIdField().orElseThrow();
    String id = pathParameters.get(idField.getName());
    if (id == null) {
      id = routingContext.request().getParam((String) idField.getName());
    }
    if (id == null) {
      throw new IllegalArgumentException("Id nust not be null");
    }
    Map<String, Object> record = dataService.findOneRecord(datasourceName, modelName, id, true);
    routingContext.response()
      .putHeader("Content-Type", "application/json")
      .end(JsonUtils.getInstance().stringify(record));
  }

  private void doList(RoutingContext routingContext, Map<String, String> pathParameters, ApiInfo apiInfo) {
    Map<String, Object> meta = (Map<String, Object>) apiInfo.getMeta();
    String datasourceName = (String) meta.get("datasource");
    String modelName = (String) meta.get("model");
    boolean paging = (Boolean) meta.get("paging");
    String filter = routingContext.request().getParam("filter");
    String sort = routingContext.request().getParam("sort");
    Map<String, Object> result = new HashMap<>();
    if (paging) {
      int current = Integer.parseInt(routingContext.request().getParam("current", "1"));
      int pageSize = Integer.parseInt(routingContext.request().getParam("pageSize", "30"));
      List<Map<String, Object>> list = dataService.findRecords(datasourceName, modelName,
        current, pageSize, filter, sort, true);
      long total = dataService.countRecords(datasourceName, modelName, filter);
      result.put("total", total);
      result.put("list", list);
    } else {
      List<Map<String, Object>> list = dataService.findRecords(datasourceName, modelName,
        null, null, filter, sort, true);
      result.put("list", list);
    }
    routingContext.response()
      .putHeader("Content-Type", "application/json")
      .end(JsonUtils.getInstance().stringify(result));
  }

  public void log(RoutingContext routingContext, Runnable runnable) {
    ApiLog apiLog = new ApiLog();
    apiLog.setLevel(LogLevel.INFO.name());
    LogData apiData = new LogData();
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
      apiLog.setLevel(LogLevel.ERROR.name());
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
