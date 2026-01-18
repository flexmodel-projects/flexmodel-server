package dev.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.ApiRuntimeApplicationService;
import dev.flexmodel.application.dto.LogStatResponse;
import dev.flexmodel.application.dto.PageDTO;
import dev.flexmodel.codegen.entity.ApiRequestLog;
import dev.flexmodel.domain.model.api.LogStat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author cjbi
 */
@Tag(name = "接口日志", description = "接口日志管理")
@Path("/v1/projects/{projectId}/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiLogResource {

  @Inject
  ApiRuntimeApplicationService apiRuntimeApplicationService;

  @Parameter(name = "current", description = "当前页，默认值：1", examples = {@ExampleObject(value = "1")}, in = ParameterIn.QUERY)
  @Parameter(name = "pageSize", description = "第几页，默认值：15", examples = {@ExampleObject(value = "15")}, in = ParameterIn.QUERY)
  @Parameter(name = "keyword", description = "关键字", in = ParameterIn.QUERY)
  @Parameter(name = "dateRange", description = "日期范围", examples = {@ExampleObject(value = "2025-01-01 00:00:00,2025-12-31 23:59:59")}, in = ParameterIn.QUERY)
  @Parameter(name = "level", description = "日志等级", examples = {@ExampleObject(value = "INFO")}, in = ParameterIn.QUERY)
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        properties = {
          @SchemaProperty(name = "total", description = "总数"),
          @SchemaProperty(name = "list", description = "日志列表", type = SchemaType.ARRAY, implementation = ApiLogSchema.class)
        }
      )
    )
    })
  @Operation(summary = "获取接口日志列表")
  @GET
  public PageDTO<ApiRequestLog> findApiLogs(@PathParam("projectId") String projectId,
                                            @QueryParam("page") @DefaultValue("1") int page,
                                            @QueryParam("size") @DefaultValue("50") int size,
                                            @QueryParam("keyword") String keyword,
                                            @QueryParam("dateRange") String dateRange,
                                            @QueryParam("isSuccess") Boolean isSuccess
  ) {
    RequestResult result = parseQuery(dateRange, isSuccess);
    return apiRuntimeApplicationService.findApiLogs(projectId, page, size, keyword, result.startDate(), result.endDate(), isSuccess);
  }

  @Parameter(name = "keyword", description = "关键字", in = ParameterIn.QUERY)
  @Parameter(name = "dateRange", description = "日期范围", examples = {@ExampleObject(value = "2022-01-01 00:00:00,2022-01-01 23:59:59")}, in = ParameterIn.QUERY)
  @Parameter(name = "level", description = "日志等级，支持传多个通过\",\"分隔", examples = {@ExampleObject(value = "INFO")}, in = ParameterIn.QUERY)
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        type = SchemaType.ARRAY,
        implementation = ApiLogStatSchema.class
      )
    )})
  @Operation(summary = "统计接口日志")
  @GET
  @Path("/stat")
  public LogStatResponse stat(@PathParam("projectId") String projectId,
                              @QueryParam("keyword") String keyword,
                              @QueryParam("dateRange") String dateRange,
                              @QueryParam("isSuccess") Boolean isSuccess) {
    RequestResult result = parseQuery(dateRange, isSuccess);
    return apiRuntimeApplicationService.stat(projectId, keyword, result.startDate(), result.endDate(), isSuccess);
  }

  private static RequestResult parseQuery(String dateRange, Boolean isSuccess) {
    LocalDateTime startDate = null;
    LocalDateTime endDate = null;
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
    return new RequestResult(startDate, endDate, isSuccess);
  }

  private record RequestResult(LocalDateTime startDate, LocalDateTime endDate, Boolean isSuccess) {
  }

  @Schema(
    description = "日志统计",
    properties = {
      @SchemaProperty(name = "date", description = "日期"),
      @SchemaProperty(name = "total", description = "总数"),
    }
  )
  public static class ApiLogStatSchema extends LogStat {

  }

  @Schema(
    description = "接口日志",
    properties = {
      @SchemaProperty(name = "id", description = "ID"),
      @SchemaProperty(name = "uri", description = "请求标识"),
      @SchemaProperty(name = "level", description = "等级"),
      @SchemaProperty(name = "createdAt", description = "创建日期", readOnly = true),
      @SchemaProperty(name = "data", description = "数据"),
    }
  )
  public static class ApiLogSchema extends ApiRequestLog {
  }


}
