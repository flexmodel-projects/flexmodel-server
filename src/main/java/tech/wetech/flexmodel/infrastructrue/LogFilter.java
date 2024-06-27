package tech.wetech.flexmodel.infrastructrue;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.domain.model.api.ApiLog;
import tech.wetech.flexmodel.domain.model.api.ApiLogService;
import tech.wetech.flexmodel.util.JsonUtils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * @author cjbi
 */
@Slf4j
@Provider
public class LogFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private final ThreadLocal<Long> reqBeginTime = new ThreadLocal<>();

  @Inject
  ApiLogService apiLogService;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    reqBeginTime.set(System.currentTimeMillis());
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    Long beginTime = reqBeginTime.get();
    long execTime;
    if (beginTime != null) {
      execTime = System.currentTimeMillis() - beginTime;
    } else {
      execTime = -1L;
    }
    CompletableFuture.runAsync(() -> {
      ApiLog apiLog = new ApiLog();
      ApiLog.Data apiData = new ApiLog.Data();
      apiLog.setData(apiData);
      apiData.setMethod(requestContext.getMethod());
      apiData.setPath(requestContext.getUriInfo().getPath());
      apiData.setReferer(requestContext.getHeaders().getFirst("Referer"));
//      apiData.setRemoteIp(null);
      apiData.setUserAgent(requestContext.getHeaders().getFirst("User-Agent"));
      int statusCode = responseContext.getStatus();
      String reasonPhrase = responseContext.getStatusInfo().getReasonPhrase();
      apiData.setStatus(statusCode);
      apiData.setMessage(reasonPhrase);
      apiLog.setLevel(ApiLog.Level.INFO);
      if (statusCode >= 400 && statusCode < 500) {
        apiLog.setLevel(ApiLog.Level.WARN);
        apiData.setErrors(JsonUtils.getInstance().stringify(responseContext.getEntity()));
      } else if (statusCode >= 500) {
        apiLog.setLevel(ApiLog.Level.ERROR);
        apiData.setErrors(JsonUtils.getInstance().stringify(responseContext.getEntity()));
      }
      apiData.setExecTime(execTime);
      apiLog.setUri(apiData.getMethod() + " " + apiData.getPath());
      apiLogService.create(apiLog);
    });
  }
}
