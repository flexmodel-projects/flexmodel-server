package tech.wetech.flexmodel.infrastructrue;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.codegen.enumeration.LogLevel;
import tech.wetech.flexmodel.domain.model.api.ApiLogService;
import tech.wetech.flexmodel.domain.model.api.LogData;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.domain.model.settings.SettingsService;
import tech.wetech.flexmodel.util.JsonUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author cjbi
 */
@Slf4j
@Provider
public class LogFilter implements ContainerRequestFilter, ContainerResponseFilter {

  @Inject
  ApiLogService apiLogService;

  @Inject
  SettingsService settingsService;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    requestContext.setProperty("startTime", System.currentTimeMillis());
    try {
      byte[] bytes = requestContext.getEntityStream().readAllBytes();
      if (bytes.length > 0) {
        String body = new String(bytes);
        requestContext.setProperty("requestBody", body);
      }
      requestContext.setEntityStream(new ByteArrayInputStream(bytes));
      //This is your POST Body as String
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    Long beginTime = (Long) requestContext.getProperty("startTime");
    long execTime;
    if (beginTime != null) {
      execTime = System.currentTimeMillis() - beginTime;
    } else {
      execTime = -1L;
    }
    CompletableFuture.runAsync(() -> {
      Settings settings = settingsService.getSettings();
      if (settings.getLog().isConsoleLoggingEnabled() &&
          !requestContext.getUriInfo().getPath().startsWith("/api/logs")
      ) {
        saveLog(requestContext, responseContext, execTime);
      }
    });
  }

  private void saveLog(ContainerRequestContext requestContext, ContainerResponseContext responseContext, long execTime) {
    ApiLog apiLog = new ApiLog();
    LogData apiData = new LogData();
    apiLog.setData(apiData);
    apiData.setMethod(requestContext.getMethod());
    apiData.setPath(requestContext.getUriInfo().getPath());
    apiData.setUrl(requestContext.getUriInfo().getRequestUri().toString());
//      apiData.setRemoteIp(null);
    int statusCode = responseContext.getStatus();
    String reasonPhrase = responseContext.getStatusInfo().getReasonPhrase();
    apiData.setStatus(statusCode);
    apiData.setMessage(reasonPhrase);
    apiLog.setLevel(LogLevel.INFO);
    Map<String, Object> request = new HashMap<>();
    apiData.setRequest(request);
    request.put("headers", requestContext.getHeaders());
    request.put("body", requestContext.getProperty("requestBody"));

    if (statusCode >= 400 && statusCode < 500) {
      apiLog.setLevel(LogLevel.WARN);
      apiData.setErrors(JsonUtils.getInstance().stringify(responseContext.getEntity()));
    } else if (statusCode >= 500) {
      apiLog.setLevel(LogLevel.ERROR);
      apiData.setErrors(JsonUtils.getInstance().stringify(responseContext.getEntity()));
    }
    apiData.setExecTime(execTime);
    apiLog.setUri(apiData.getMethod() + " " + apiData.getPath());
    apiLogService.create(apiLog);
  }
}
