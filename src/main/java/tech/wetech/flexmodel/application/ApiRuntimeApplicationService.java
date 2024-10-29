package tech.wetech.flexmodel.application;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
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
    log(routingContext, () -> {
      List<ApiInfo> apis = apiInfoService.findList();
      for (ApiInfo apiInfo : apis) {
        Map<String, Object> meta = (Map<String, Object>) apiInfo.getMeta();
        UriTemplate uriTemplate = new UriTemplate("/api/v1" + apiInfo.getPath());
        Map<String, String> pathParameters = uriTemplate.match(new UriTemplate(routingContext.normalizedPath()));
        String method = routingContext.request().method().name();
        if (pathParameters != null && method.equals(apiInfo.getMethod())) {
          log.debug("Matched request for api: {}", apiInfo);
          try {
            Boolean rateLimitingEnabled = (Boolean) meta.getOrDefault("rateLimitingEnabled", false);
            Settings settings = settingsService.getSettings();
            if (rateLimitingEnabled || settings.getSecurity().isRateLimitingEnabled()) {
              int maxRequestCount = settings.getSecurity().getMaxRequestCount();
              int intervalInSeconds = settings.getSecurity().getIntervalInSeconds();
              ApiRateLimiterHolder.ApiRateLimiter apiRateLimiter;
              if (rateLimitingEnabled) {
                maxRequestCount = (int) meta.get("maxRequestCount");
                intervalInSeconds = (int) meta.get("intervalInSeconds");
                apiRateLimiter = ApiRateLimiterHolder.getApiRateLimiter(apiInfo.getMethod() + ":" + apiInfo.getPath(), maxRequestCount, intervalInSeconds);
              } else {
                apiRateLimiter = ApiRateLimiterHolder.getApiRateLimiter(apiInfo.getMethod() + ":" + apiInfo.getPath() + "@default", maxRequestCount, intervalInSeconds);
              }
              if (!apiRateLimiter.tryAcquire()) {
                Map<String, Object> result = new HashMap<>();
                result.put("messasge", "Too many requests.");
                result.put("code", -1);
                result.put("success", false);
                routingContext.response()
                  .putHeader("Content-Type", "application/json")
                  .end(JsonUtils.getInstance().stringify(result));
                return;
              }
            }
          } catch (Exception e) {
            log.error("Rate limiting error: {}", e.getMessage(), e);
          }
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
    });
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
