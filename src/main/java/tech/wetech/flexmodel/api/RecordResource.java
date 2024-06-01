package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import tech.wetech.flexmodel.application.DataApplicationService;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@Path("/api/datasources/{datasourceName}/models/{modelName}/records")
public class RecordResource {

  @PathParam("datasourceName")
  String datasourceName;

  @PathParam("modelName")
  String modelName;

  @Inject
  DataApplicationService dataApplicationService;

  @GET
  public List<Map<String, Object>> findRecords() {
    return dataApplicationService.findList(datasourceName, modelName);
  }

}
