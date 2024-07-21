package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import tech.wetech.flexmodel.application.DataApplicationService;
import tech.wetech.flexmodel.application.dto.PageDTO;

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
  public PageDTO<Map<String, Object>> findPagingRecords(
    @QueryParam("current") @DefaultValue("1") int current,
    @QueryParam("pageSize") @DefaultValue("15") int pageSize,
    @QueryParam("filter") String filter,
    @QueryParam("deep") @DefaultValue("false") boolean deep,
    @QueryParam("sort") String sort
  ) {
    return dataApplicationService.findPagingRecords(datasourceName, modelName, current, pageSize, filter, sort, deep);
  }

  @GET
  @Path("/{id}")
  public Map<String, Object> findOneRecord(@PathParam("id") String id, @QueryParam("deep") @DefaultValue("false") boolean deep) {
    return dataApplicationService.findOneRecord(datasourceName, modelName, id, deep);
  }

  @POST
  public Map<String, Object> createRecord(Map<String, Object> record) {
    Map<String, Object> result = dataApplicationService.createRecord(datasourceName, modelName, record);
    return result;
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
