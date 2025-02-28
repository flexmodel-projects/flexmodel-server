package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.ApiLogApplicationService;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.codegen.StringUtils;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.codegen.enumeration.LogLevel;
import tech.wetech.flexmodel.domain.model.api.LogStat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "接口日志", description = "接口日志管理")
@Path(BASE_PATH + "/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiLogResource {

  @Inject
  ApiLogApplicationService apiLogApplicationService;

  @Operation(summary = "获取接口日志列表")
  @GET
  public PageDTO<ApiLog> findApiLogs(@QueryParam("current") @DefaultValue("1") int current,
                                     @QueryParam("pageSize") @DefaultValue("50") int pageSize,
                                     @QueryParam("keyword") String keyword,
                                     @QueryParam("dateRange") String dateRange,
                                     @QueryParam("level") String levelStr
  ) {
    RequestResult result = parseQuery(dateRange, levelStr);
    return apiLogApplicationService.findApiLogs(current, pageSize, keyword, result.startDate(), result.endDate(), result.levels());
  }

  @Operation(summary = "统计接口日志")
  @GET
  @Path("/stat")
  public List<LogStat> stat(@QueryParam("pageSize") @DefaultValue("50") int pageSize,
                            @QueryParam("keyword") String keyword,
                            @QueryParam("dateRange") String dateRange,
                            @QueryParam("level") String levelStr) {
    RequestResult result = parseQuery(dateRange, levelStr);
    return apiLogApplicationService.stat(keyword, result.startDate(), result.endDate(), result.levels());
  }

  private static RequestResult parseQuery(String dateRange, String levelStr) {
    LocalDateTime startDate = null;
    LocalDateTime endDate = null;
    Set<LogLevel> levels = null;
    if (dateRange != null) {
      try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String[] dateRangeArr = dateRange.split(",");
        startDate = LocalDateTime.parse(dateRangeArr[0], formatter);
        endDate = LocalDateTime.parse(dateRangeArr[1], formatter);
      } catch (Exception e) {
        startDate = null;
        endDate = null;
      }
    }
    if (!StringUtils.isBlank(levelStr)) {
      levels = Stream.of(levelStr.split(",")).map(LogLevel::valueOf).collect(Collectors.toSet());
    }
    return new RequestResult(startDate, endDate, levels);
  }

  private record RequestResult(LocalDateTime startDate, LocalDateTime endDate, Set<LogLevel> levels) {
  }

}
