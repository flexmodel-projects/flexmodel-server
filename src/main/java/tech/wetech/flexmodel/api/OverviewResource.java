package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.OverviewApplicationService;
import tech.wetech.flexmodel.application.dto.OverviewDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static tech.wetech.flexmodel.api.Resources.ROOT_PATH;

/**
 * @author cjbi
 */
@Tag(name = "概述", description = "首页概览")
@Path(ROOT_PATH + "/overview")
public class OverviewResource {

  @Inject
  OverviewApplicationService overviewApplicationService;

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        properties = {
          @SchemaProperty(name = "queryCount", description = "查询接口定义统计"),
          @SchemaProperty(name = "mutationCount", description = "变更接口定义统计"),
          @SchemaProperty(name = "subscribeCount", description = "订阅接口定义统计"),
          @SchemaProperty(name = "dataSourceCount", description = "数据源统计"),
        }
      )
    )
    })
  @Parameter(name = "dateRange", description = "日期范围", example = "2025-01-01 00:00:00,2025-12-31 23:59:59", in = ParameterIn.QUERY)
  @Operation(summary = "获取概述")
  @GET
  public OverviewDTO get(@QueryParam("dateRange") String dateRange) {
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
    return overviewApplicationService.getOverview(startDate, endDate);
  }

}
