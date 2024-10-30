package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.application.ApiLogApplicationService;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.domain.model.api.LogStat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
@Path("/api/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiLogResource {

  @Inject
  ApiLogApplicationService apiLogApplicationService;

  @GET
  public PageDTO<ApiLog> findApiLogs(@QueryParam("current") @DefaultValue("1") int current,
                                     @QueryParam("pageSize") @DefaultValue("50") int pageSize,
                                     @QueryParam("keyword") String keyword,
                                     @QueryParam("dateRange") String dateRange,
                                     @QueryParam("level") String levelStr
  ) {
    RequestResult result = parseQuery(dateRange, levelStr);
    return apiLogApplicationService.findApiLogs(current, pageSize, keyword, result.startDate(), result.endDate(), result.level());
  }

  @GET
  @Path("/stat")
  public List<LogStat> stat(@QueryParam("pageSize") @DefaultValue("50") int pageSize,
                            @QueryParam("keyword") String keyword,
                            @QueryParam("dateRange") String dateRange,
                            @QueryParam("level") String levelStr) {
    RequestResult result = parseQuery(dateRange, levelStr);
    return apiLogApplicationService.stat(keyword, result.startDate(), result.endDate(), result.level());
  }

  private static RequestResult parseQuery(String dateRange, String levelStr) {
    LocalDateTime startDate = null;
    LocalDateTime endDate = null;
    Set<String> level = null;
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
    if (levelStr != null) {
      level = Set.of(levelStr.split(","));
    }
    return new RequestResult(startDate, endDate, level);
  }

  private record RequestResult(LocalDateTime startDate, LocalDateTime endDate, Set<String> level) {
  }

}
