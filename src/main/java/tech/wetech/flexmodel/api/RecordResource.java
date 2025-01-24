package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import tech.wetech.flexmodel.application.DataApplicationService;
import tech.wetech.flexmodel.application.dto.PageDTO;

import java.util.Map;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Path(BASE_PATH +"/datasources/{datasourceName}/models/{modelName}/records")
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
    @QueryParam("nestedQuery") @DefaultValue("false") boolean nestedQuery,
    @QueryParam("sort") String sort
  ) {
    return dataApplicationService.findPagingRecords(datasourceName, modelName, current, pageSize, filter, sort, nestedQuery);
  }

  @GET
  @Path("/{id}")
  public Map<String, Object> findOneRecord(@PathParam("id") String id, @QueryParam("nestedQuery") @DefaultValue("false") boolean nestedQuery) {
    return dataApplicationService.findOneRecord(datasourceName, modelName, id, nestedQuery);
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
