package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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
    return dataApplicationService.findRecords(datasourceName, modelName);
  }

  @GET
  @Path("/{id}")
  public Map<String, Object> findOneRecord(@PathParam("id") String id) {
    return dataApplicationService.findOneRecord(datasourceName, modelName, id);
  }

  @POST
  public Map<String, Object> createRecord(Map<String, Object> record) {
    return dataApplicationService.createRecord(datasourceName, modelName, record);
  }

  @PUT
  @Path("/{id}")
  public Map<String, Object> updateRecord(@PathParam("id") String id, Map<String, Object> record) {
    return dataApplicationService.updateRecord(datasourceName, modelName, id, record);
  }

  @DELETE
  @Path("/{id}")
  public void deleteRecord(@PathParam("id") String id) {
    dataApplicationService.deleteRecord(datasourceName, modelName, id);
  }

}
