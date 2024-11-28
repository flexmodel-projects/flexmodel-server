package tech.wetech.flexmodel.application;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.ApiInfo;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.domain.model.api.*;
import tech.wetech.flexmodel.domain.model.data.DataService;
import tech.wetech.flexmodel.domain.model.idp.IdentityProviderService;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.domain.model.settings.SettingsService;
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
  @Inject
  SettingsService settingsService;

  @Inject
  EventBus eventBus;

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

  @SuppressWarnings("all")
  public void accept(RoutingContext routingContext) {
    log(routingContext, () -> doRequest(routingContext));
  }

  private void doRequest(RoutingContext routingContext) {
    List<ApiInfo> apis = apiInfoService.findList();
    boolean isMatching = false;
    for (ApiInfo apiInfo : apis) {
      Map<String, Object> meta = (Map<String, Object>) apiInfo.getMeta();
      UriTemplate uriTemplate = new UriTemplate("/api/v1" + apiInfo.getPath());
      Map<String, String> pathParameters = uriTemplate.match(new UriTemplate(routingContext.normalizedPath()));
      String method = routingContext.request().method().name();
      if (pathParameters != null && method.equals(apiInfo.getMethod())) {
        // 匹配成功
        isMatching = true;
        log.debug("Matched request for api: {}", apiInfo);
        if (isRateLimiting(routingContext, apiInfo, meta)) return;
        boolean isAuth = (boolean) meta.get("auth");
        if (isAuth) {
          String identityProvider = (String) meta.get("identityProvider");
          String authorization = Objects.toString(routingContext.request().getHeader("Authorization"), "");
          String token = authorization.replace("Bearer", "").trim();
          boolean active = identityProviderService.checkToken(identityProvider, token);
          if (!active) {
            sendAuthFail(routingContext);
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
        break;
      }
    }

    Settings settings = settingsService.getSettings();
    if (settings.getSecurity().isGraphqlEndpointEnabled()) {
      UriTemplate uriTemplate = new UriTemplate("/api/v1" + settings.getSecurity().getGraphqlEndpointPath());
      Map<String, String> pathParameters = uriTemplate.match(new UriTemplate(routingContext.normalizedPath()));
      String method = routingContext.request().method().name();
      if (pathParameters != null && method.equals("POST")) {
        isMatching = true;
        // 匹配成功
        log.debug("Matched request for api: {}", settings.getSecurity().getGraphqlEndpointPath());
        if (isRateLimiting(routingContext, null, null)) return;
        String identityProvider = settings.getSecurity().getGraphqlEndpointIdentityProvider();
        // 鉴权
        if (identityProvider != null) {
          String authorization = Objects.toString(routingContext.request().getHeader("Authorization"), "");
          String token = authorization.replace("Bearer", "").trim();
          boolean active = identityProviderService.checkToken(identityProvider, token);
          if (!active) {
            sendAuthFail(routingContext);
            return;
          }
        }
        String bodyString = routingContext.body().asString();
        Map body = (Map) JsonUtils.getInstance().parseToObject(bodyString, Map.class);
        ExecutionResult result = execute((String) body.get("operationName"), (String) body.get("query"), (Map<String, Object>) body.get("variables"));
        routingContext.response()
          .putHeader("Content-Type", "application/json")
          .end(JsonUtils.getInstance().stringify(result));
      }
    }

    // 未找到地址
    if (!isMatching) {
      sendNotFoundError(routingContext);
    }
  }

  private boolean isRateLimiting(RoutingContext routingContext, ApiInfo apiInfo, Map<String, Object> meta) {
    try {
      Settings settings = settingsService.getSettings();
      if (settings.getSecurity().isRateLimitingEnabled()) {
        log.debug("Global Rate limiting enabled.");
        ApiRateLimiterHolder.ApiRateLimiter apiRateLimiter = ApiRateLimiterHolder.getApiRateLimiter("__DEFAULT",
          settings.getSecurity().getMaxRequestCount(),
          settings.getSecurity().getIntervalInSeconds());
        if (!apiRateLimiter.tryAcquire()) {
          Map<String, Object> result = new HashMap<>();
          result.put("messasge", "Too many requests.");
          result.put("code", -1);
          result.put("success", false);
          routingContext.response()
            .putHeader("Content-Type", "application/json")
            .end(JsonUtils.getInstance().stringify(result));
          return true;
        }
      }
      Boolean rateLimitingEnabled = (Boolean) meta.getOrDefault("rateLimitingEnabled", false);
      if (rateLimitingEnabled) {
        log.debug("Rate limiting enabled for api: {}", apiInfo);
        int maxRequestCount = settings.getSecurity().getMaxRequestCount();
        int intervalInSeconds = settings.getSecurity().getIntervalInSeconds();
        maxRequestCount = (int) meta.get("maxRequestCount");
        intervalInSeconds = (int) meta.get("intervalInSeconds");
        ApiRateLimiterHolder.ApiRateLimiter apiRateLimiter = ApiRateLimiterHolder.getApiRateLimiter(
          apiInfo.getMethod() + ":" + apiInfo.getPath(),
          maxRequestCount,
          intervalInSeconds);
        if (!apiRateLimiter.tryAcquire()) {
          Map<String, Object> result = new HashMap<>();
          result.put("messasge", "Too many requests.");
          result.put("code", -1);
          result.put("success", false);
          routingContext.response()
            .putHeader("Content-Type", "application/json")
            .end(JsonUtils.getInstance().stringify(result));
          return true;
        }
      }
    } catch (Exception e) {
      log.error("Rate limiting error: {}", e.getMessage(), e);
    }
    return false;
  }

  private void sendAuthFail(RoutingContext routingContext) {
    Map<String, Object> result = new HashMap<>();
    result.put("messasge", "Authentication failed.");
    result.put("code", -1);
    result.put("success", false);
    routingContext.response()
      .setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
      .putHeader("Content-Type", "application/json")
      .end(JsonUtils.getInstance().stringify(result));
  }

  private void sendNotFoundError(RoutingContext routingContext) {
    Map<String, Object> result = new HashMap<>();
    result.put("messasge", "not found");
    result.put("code", -1);
    result.put("success", false);
    routingContext.response()
      .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
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
      eventBus.publish("request.logging", apiLog);
    }

  }

}
