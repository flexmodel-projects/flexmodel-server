package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.application.ApiRuntimeApplicationService;
import tech.wetech.flexmodel.domain.model.api.ApiLog;
import tech.wetech.flexmodel.domain.model.api.LogStat;

import java.util.List;

/**
 * @author cjbi
 */
@Path("/api/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiLogResource {

  @Inject
  ApiRuntimeApplicationService apiRuntimeApplicationService;

  @GET
  public List<ApiLog> findApiList(@QueryParam("current" ) @DefaultValue("1" ) int current,
                                  @QueryParam("pageSize" ) @DefaultValue("50" ) int pageSize,
                                  @QueryParam("filter" ) String filter) {
    return apiRuntimeApplicationService.findApiLogs(filter, current, pageSize);
  }

  @GET
  @Path("/stat" )
  public List<LogStat> stat(@QueryParam("filter" ) String filter) {
    return apiRuntimeApplicationService.stat(filter);
  }

}
