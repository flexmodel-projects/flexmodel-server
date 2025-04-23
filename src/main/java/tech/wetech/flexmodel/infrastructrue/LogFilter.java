package tech.wetech.flexmodel.infrastructrue;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.ApiRequestLog;
import tech.wetech.flexmodel.domain.model.api.ApiLogRequestService;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.domain.model.settings.SettingsService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * @author cjbi
 */
@Slf4j
@Provider
public class LogFilter implements ContainerRequestFilter, ContainerResponseFilter {

  @Inject
  ApiLogRequestService apiLogService;

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
    ApiRequestLog apiLog = new ApiRequestLog();
    apiLog.setHttpMethod(requestContext.getMethod());
    apiLog.setUrl(requestContext.getUriInfo().getRequestUri().toString());
    apiLog.setPath(requestContext.getUriInfo().getPath());
    apiLog.setRequestHeaders(requestContext.getHeaders());
    apiLog.setRequestBody(requestContext.getProperty("requestBody"));
//      apiData.setRemoteIp(null);
    int statusCode = responseContext.getStatus();
    apiLog.setStatusCode(statusCode);
    if (statusCode >= 500) {
      apiLog.setIsSuccess(false);
    }
    apiLog.setResponseTime((int) execTime);
    apiLogService.create(apiLog);
  }
}
