package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.OverviewApplicationService;
import tech.wetech.flexmodel.application.dto.OverviewDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "概述", description = "首页概览")
@Path(BASE_PATH + "/overview")
public class OverviewResource {

  @Inject
  OverviewApplicationService overviewApplicationService;

  @Operation(summary ="获取概述")
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
