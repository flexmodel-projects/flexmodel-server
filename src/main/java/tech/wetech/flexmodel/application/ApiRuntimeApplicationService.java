package tech.wetech.flexmodel.application;

import graphql.ExecutionResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Request;
import org.slf4j.Logger;
import tech.wetech.flexmodel.application.consumer.SettingsEventConsumer;
import tech.wetech.flexmodel.application.dto.LogStatResponse;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.domain.model.api.execution.ExecutionStrategy;
import tech.wetech.flexmodel.domain.model.api.execution.ExecutionStrategyFactory;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.codegen.entity.ApiRequestLog;
import tech.wetech.flexmodel.codegen.entity.IdentityProvider;
import tech.wetech.flexmodel.codegen.enumeration.ApiType;
import tech.wetech.flexmodel.domain.model.api.*;
import tech.wetech.flexmodel.domain.model.data.DataService;
import tech.wetech.flexmodel.domain.model.flow.shared.util.HttpScriptContext;
import tech.wetech.flexmodel.domain.model.idp.IdentityProviderService;
import tech.wetech.flexmodel.domain.model.idp.provider.Provider;
import tech.wetech.flexmodel.domain.model.idp.provider.ValidateParam;
import tech.wetech.flexmodel.domain.model.idp.provider.ValidateResult;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;
import tech.wetech.flexmodel.domain.model.auth.ProjectService;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.domain.model.settings.SettingsService;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.shared.FlexmodelConfig;
import tech.wetech.flexmodel.shared.SessionContextHolder;
import tech.wetech.flexmodel.shared.matchers.UriTemplate;
import tech.wetech.flexmodel.shared.utils.JsonUtils;
import tech.wetech.flexmodel.shared.utils.PatternMatchUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@SuppressWarnings("all")
@ActivateRequestContext
@ApplicationScoped
public class ApiRuntimeApplicationService {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(ApiRuntimeApplicationService.class);
  @Inject
  ApiDefinitionService apiDefinitionService;

  @Inject
  IdentityProviderService identityProviderService;

  @Inject
  ApiRequestLogService apiLogService;

  @Inject
  ModelService modelService;

  @Inject
  DataService dataService;

  @Inject
  GraphQLManger graphQLManger;

  @Inject
  SettingsService settingsService;

  @Inject
  EventBus eventBus;

  @Inject
  FlexmodelConfig config;

  @Inject
  ExecutionStrategyFactory executionStrategyFactory;

  @Inject
  Request request;

  @Inject
  ProjectService projectService;

  public PageDTO<ApiRequestLog> findApiLogs(String projectId, int current, int pageSize, String keyword, LocalDateTime startDate, LocalDateTime endDate, Boolean isSuccess) {
    List<ApiRequestLog> list = apiLogService.find(projectId, getCondition(keyword, startDate, endDate, isSuccess), current, pageSize);
    long total = apiLogService.count(projectId, getCondition(keyword, startDate, endDate, isSuccess));
    return new PageDTO<>(list, total);
  }

  public LogStatResponse stat(String projectId, String keyword, LocalDateTime startDate, LocalDateTime endDate, Boolean isSuccess) {

    LogStatResponse.ApiChart statDTO = null;
    List<String> dateList = new ArrayList<>();
    String fmt = "yyyy-MM-dd HH:00:00";
    if (startDate == null) {
      startDate = LocalDateTime.now().minusDays(7);
    }
    if (endDate == null) {
      endDate = LocalDateTime.now();
    }
    if (ChronoUnit.DAYS.between(startDate, endDate) <= 1) {
      fmt = "yyyy-MM-dd HH:00:00";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusHours(1);
      }
    } else if (ChronoUnit.DAYS.between(startDate, endDate) > 1 && ChronoUnit.DAYS.between(startDate, endDate) <= 7) {
      fmt = "yyyy-MM-dd";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusDays(1);
      }

    } else if (ChronoUnit.DAYS.between(startDate, endDate) > 7 && ChronoUnit.DAYS.between(startDate, endDate) <= 31) {
      fmt = "yyyy-MM-dd";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusDays(1);
      }
    } else if (ChronoUnit.DAYS.between(startDate, endDate) > 31 && ChronoUnit.DAYS.between(startDate, endDate) <= 365) {
      fmt = "yyyy-MM";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusMonths(1);
      }
    } else {
      fmt = "yyyy";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusYears(1);
      }
    }

    Predicate condition = getCondition(keyword, startDate, endDate, isSuccess);

    Map<String, Long> successMap = apiLogService.stat(projectId, condition.and(field(ApiRequestLog::getIsSuccess).eq(true)), fmt).stream().collect(Collectors.toMap(LogStat::getDate, LogStat::getTotal));
    Map<String, Long> failMap = apiLogService.stat(projectId, condition.and(field(ApiRequestLog::getIsSuccess).eq(false)), fmt).stream().collect(Collectors.toMap(LogStat::getDate, LogStat::getTotal));
    statDTO = new LogStatResponse.ApiChart();
    List<Long> successData = new ArrayList<>();
    List<Long> failData = new ArrayList<>();
    for (String date : dateList) {
      successData.add(successMap.getOrDefault(date, 0L));
      failData.add(failMap.getOrDefault(date, 0L));
    }
    statDTO.setDateList(dateList);
    statDTO.setSuccessData(successData);
    statDTO.setFailData(failData);

    List<LogStat> stat = apiLogService.stat(projectId, condition, fmt);

    List<LogApiRank> apiRankList = apiLogService.ranking(projectId, condition);

    return LogStatResponse.builder().apiStatList(stat).apiRankingList(apiRankList).apiChart(statDTO).build();
  }

  private static Predicate getCondition(String keyword, LocalDateTime startDate, LocalDateTime endDate, Boolean isSuccess) {
    Predicate condition = Expressions.TRUE;
    if (keyword != null) {
      condition = condition.and(field(ApiRequestLog::getRequestBody).contains(keyword));
    }
    if (startDate != null && endDate != null) {
      condition = condition.and(field(ApiRequestLog::getCreatedAt).between(startDate, endDate));
    }
    if (isSuccess != null) {
      condition = condition.and(field(ApiRequestLog::getIsSuccess).eq(isSuccess));
    }
    return condition;
  }

  @SuppressWarnings("all")
  public void accept(RoutingContext routingContext) {
    log(routingContext, () -> doRequest(routingContext));
  }

  private void doRequest(RoutingContext routingContext) {
    boolean isMatching = false;
    Map<String, String> projectIdMap = new UriTemplate("/api/{projectId}/.*").match(new UriTemplate(routingContext.normalizedPath()));
    String projectId = projectIdMap.get("projectId");
    SessionContextHolder.setProjectId(projectId);
    List<ApiDefinition> apis = apiDefinitionService.findAll(projectId);
    Settings settings = settingsService.getSettings();
    // 从apiDefinition处理请求
    for (ApiDefinition apiDefinition : apis) {
      if (apiDefinition.getType() != ApiType.API) {
        continue;
      }
      ApiDefinitionMeta meta = JsonUtils.getInstance().convertValue(apiDefinition.getMeta(), ApiDefinitionMeta.class);
      UriTemplate uriTemplate = new UriTemplate(config.apiRootPath() + "/{projectId}" + apiDefinition.getPath());
      Map<String, String> pathParameters = uriTemplate.match(new UriTemplate(routingContext.normalizedPath()));
      String method = routingContext.request().method().name();
      if (pathParameters != null && method.equals(apiDefinition.getMethod())) {
        // 匹配成功
        isMatching = true;
        log.debug("Matched request for api: {}", apiDefinition);
        if (isRateLimiting(routingContext, apiDefinition, meta)) return;
        boolean isAuth = meta.isAuth();
        if (isAuth) {
          String identityProvider = meta.getIdentityProvider();
          checkToken(routingContext, identityProvider);
        }

        ApiDefinitionMeta.Execution execution = meta.getExecution();

        HttpScriptContext httpScriptContext = buildHttpScriptContext(routingContext);
        try {
          // 执行具体类型的请求
          ExecutionStrategy strategy = executionStrategyFactory.getStrategy(execution.getExecutionType());
          strategy.execute(apiDefinition, execution, pathParameters, httpScriptContext);

          int resStatus = httpScriptContext.getResponse().status();
          String resMessage = httpScriptContext.getResponse().message();
          Map<String, String> resHeaders = httpScriptContext.getResponse().headers();
          Map<String, Object> resBody = httpScriptContext.getResponse().body();

          // 添加headers
          resHeaders.forEach((key, value) -> routingContext.response().putHeader(key, value));

          routingContext.response()
            .setStatusCode(resStatus)
            .setStatusMessage(resMessage)
            .putHeader("Content-Type", "application/json")
            .end(JsonUtils.getInstance().stringify(resBody));
        } catch (Exception e) {
          log.error("Execute executionStrategy error: {}", e.getMessage(), e);
          sendBadRequestFail(routingContext, e.getMessage(), 500);
          return;
        }
        break;
      }
    }

    // 从设置中的GraphQL端点处理请求
    if (!isMatching) {
      UriTemplate uriTemplate = new UriTemplate(config.apiRootPath() + "/{projectId}" + settings.getSecurity().getGraphqlEndpointPath());
      Map<String, String> pathParameters = uriTemplate.match(new UriTemplate(routingContext.normalizedPath()));
      String method = routingContext.request().method().name();
      if (pathParameters != null && method.equals("POST")) {
        isMatching = true;
        log.debug("Matched graphql enpoint: {}", config.apiRootPath() + settings.getSecurity().getGraphqlEndpointPath());
        // 匹配成功
        if (isRateLimiting(routingContext, null, null)) return;
        String identityProvider = settings.getSecurity().getGraphqlEndpointIdentityProvider();
        // 鉴权
        if (identityProvider != null) {
          checkToken(routingContext, identityProvider);
        }
        String bodyString = routingContext.body().asString();
        Map body;
        try {
          body = (Map) JsonUtils.getInstance().parseToObject(bodyString, Map.class);
          if (body.get("query") == null) {
            sendBadRequestFail(routingContext, "query is required, e.g. { \"query\": \"query MyQuery { dev_test_aggregate_Classes { _count } }\" }", -1);
            return;
          }
        } catch (Exception e) {
          log.error("Parse body error: {}", e.getMessage(), e);
          routingContext.fail(400);
          sendBadRequestFail(routingContext, "Parse body error:" + e.getMessage(), -1);
          return;
        }
        ExecutionResult result = graphQLManger.execute(SessionContextHolder.getProjectId(), (String) body.get("operationName"), (String) body.get("query"), (Map<String, Object>) body.get("variables"));
        routingContext.response().putHeader("Content-Type", "application/json").end(JsonUtils.getInstance().stringify(result));
      }
    }

    // 从设置中的Proxy处理请求
    if (!isMatching) {
      String path = routingContext.request().path();
      for (Settings.Route route : settings.getProxy().getRoutes()) {
        if (PatternMatchUtils.simpleMatch(config.apiRootPath() + route.getPath(), path)) {
          isMatching = true;
          if (isRateLimiting(routingContext, null, null)) return;
          String targetUri = route.getTo() + path;
          log.debug("Matched request for proxy: {}", targetUri);
          // 转发请求
          forwardRequest(targetUri, routingContext.request());
          break;
        }
      }
    }

    // 未找到地址
    if (!isMatching) {
      routingContext.next();
    }
  }

  private HttpScriptContext buildHttpScriptContext(RoutingContext routingContext) {
    HttpScriptContext requestScriptContext = new HttpScriptContext();

    HttpScriptContext.Request request = new HttpScriptContext.Request(routingContext.request().method().name(),
      routingContext.normalizedPath(), multiMapToMap(routingContext.request().headers()),
      routingContext.body().asPojo(Map.class),
      multiMapToMap(routingContext.queryParams()));
    requestScriptContext.setRequest(request);
    return requestScriptContext;
  }


  private Map<String, String> multiMapToMap(MultiMap multiMap) {
    Map<String, String> map = new HashMap<>();
    multiMap.forEach((key, value) -> {
      map.put(key, value);
    });
    return map;
  }


  private void forwardRequest(String targetUri, HttpServerRequest request) {
    // 解析请求 URL 和目标地址

    // 使用 JDK HttpClient 构建请求
    HttpClient client = HttpClient.newHttpClient();

    // 构建 HttpRequest 请求
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(targetUri)).method(request.method().toString(), HttpRequest.BodyPublishers.noBody());


    // 转发请求的 headers
    for (Map.Entry<String, String> header : request.headers().entries()) {
      requestBuilder.header(header.getKey(), header.getValue());
    }

    // 转发请求的 HTTP 方法和 body
    request.bodyHandler(body -> {
      if (body.length() > 0) {
        // 对于有请求体的请求（如 POST, PUT）
        requestBuilder.method(request.method().toString(), HttpRequest.BodyPublishers.ofString(body.toString()));
      }

      // 创建 HttpRequest
      HttpRequest httpRequest = requestBuilder.build();

      // 发送请求并获取响应
      client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
        // 处理目标服务器的响应并转发回客户端
        HttpServerResponse responseToClient = request.response();
        responseToClient.setStatusCode(response.statusCode());
        HttpHeaders headers = response.headers();
        // 设置 headers
        headers.map().forEach((k, v) -> responseToClient.putHeader(k, v));
        // 设置body
        responseToClient.end(response.body());
        return null;
      }).exceptionally(e -> {
        request.response().setStatusCode(500).end("Error forwarding request: " + e.getMessage());
        return null;
      });
    });
  }

  private boolean isRateLimiting(RoutingContext routingContext, ApiDefinition apiDefinition, ApiDefinitionMeta meta) {
    try {
      Settings settings = settingsService.getSettings();
      if (settings.getSecurity().isRateLimitingEnabled()) {
        log.debug("Global Rate limiting enabled.");
        ApiRateLimiterHolder.ApiRateLimiter apiRateLimiter = ApiRateLimiterHolder.getApiRateLimiter(SettingsEventConsumer.GLOBAL_RATE_LIMIT_KEY, settings.getSecurity().getMaxRequestCount(), settings.getSecurity().getIntervalInSeconds());
        if (!apiRateLimiter.tryAcquire()) {
          Map<String, Object> result = new HashMap<>();
          result.put("messasge", "Too many requests.");
          result.put("code", -1);
          result.put("success", false);
          routingContext.response().putHeader("Content-Type", "application/json").end(JsonUtils.getInstance().stringify(result));
          return true;
        }
      }
      boolean rateLimitingEnabled = meta.isRateLimitingEnabled();
      if (rateLimitingEnabled) {
        log.debug("Rate limiting enabled for api: {}", apiDefinition);
        int maxRequestCount = settings.getSecurity().getMaxRequestCount();
        int intervalInSeconds = settings.getSecurity().getIntervalInSeconds();
        maxRequestCount = meta.getMaxRequestCount();
        intervalInSeconds = meta.getIntervalInSeconds();
        ApiRateLimiterHolder.ApiRateLimiter apiRateLimiter = ApiRateLimiterHolder.getApiRateLimiter(apiDefinition.getMethod() + ":" + apiDefinition.getPath(), maxRequestCount, intervalInSeconds);
        if (!apiRateLimiter.tryAcquire()) {
          Map<String, Object> result = new HashMap<>();
          result.put("messasge", "Too many requests.");
          result.put("code", -1);
          result.put("success", false);
          routingContext.response().putHeader("Content-Type", "application/json").end(JsonUtils.getInstance().stringify(result));
          return true;
        }
      }
    } catch (Exception e) {
      log.error("Rate limiting error: {}", e.getMessage(), e);
    }
    return false;
  }

  private void sendAuthFail(RoutingContext routingContext, ValidateResult validateResult) {
    Map<String, Object> result = new HashMap<>();
    result.put("messasge", validateResult.getMessage());
    result.put("code", -1);
    result.put("success", validateResult.isSuccess());
    routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).putHeader("Content-Type", "application/json").end(JsonUtils.getInstance().stringify(result));
  }

  private void sendNotFoundError(RoutingContext routingContext) {
    Map<String, Object> result = new HashMap<>();
    result.put("messasge", "not found");
    result.put("code", -1);
    result.put("success", false);
    routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).putHeader("Content-Type", "application/json").end(JsonUtils.getInstance().stringify(result));
  }

  private void sendBadRequestFail(RoutingContext routingContext, String msg, Integer code) {
    Map<String, Object> result = new HashMap<>();
    result.put("messasge", msg);
    result.put("code", code);
    result.put("success", false);
    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).putHeader("Content-Type", "application/json").end(JsonUtils.getInstance().stringify(result));
  }

  public void log(RoutingContext routingContext, Runnable runnable) {
    ApiRequestLog apiLog = new ApiRequestLog();
    long beginTime = System.currentTimeMillis();
    try {
      apiLog.setHttpMethod(routingContext.request().method().name());
      apiLog.setUrl(routingContext.request().uri());
      apiLog.setPath(apiLog.getHttpMethod() + " " + routingContext.request().path());
      apiLog.setRequestHeaders(routingContext.request().headers());
      apiLog.setClientIp(routingContext.request().remoteAddress().host());
      runnable.run();
    } catch (Exception e) {
      apiLog.setIsSuccess(false);
      apiLog.setErrorMessage(JsonUtils.getInstance().stringify(e));
      throw e;
    } finally {
      apiLog.setStatusCode(routingContext.response().getStatusCode());
      apiLog.setResponseTime((int) (System.currentTimeMillis() - beginTime));
      eventBus.send("request.logging", apiLog);
    }

  }

  public void checkToken(RoutingContext routingContext, String providerName) {
    String authorization = Objects.toString(routingContext.request().getHeader("Authorization"), "");
    String token = authorization.replace("Bearer", "").trim();
    if (providerName == null || token == null) {
      log.warn("Check token required parameters is null, providerName={}, token={}", providerName, token);
    }
    IdentityProvider identityProvider = identityProviderService.find(providerName);
    if (identityProvider == null) {
      log.warn("IdentityProvider is null, providerName={}, token={}", providerName, token);
      sendAuthFail(routingContext, new ValidateResult(false, "IdentityProvider is null"));
    }
    Provider provider = JsonUtils.getInstance().convertValue(identityProvider.getProvider(), Provider.class);
    ValidateParam param = new ValidateParam();

    param.setMethod(routingContext.request().method().name());
    param.setUrl(routingContext.request().path());
    param.setQuery(multiMapToMap(routingContext.request().params()));
    param.setHeaders(multiMapToMap(routingContext.request().headers()));
    ValidateResult result = provider.validate(param);
    if (!result.isSuccess()) {
      sendAuthFail(routingContext, result);
    }
  }

}
