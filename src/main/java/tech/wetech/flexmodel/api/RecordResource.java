package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.DataApplicationService;
import tech.wetech.flexmodel.application.dto.PageDTO;

import java.util.Map;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "模型数据", description = "模型数据管理")
@Path(BASE_PATH +"/datasources/{datasourceName}/models/{modelName}/records")
public class RecordResource {

  @PathParam("datasourceName")
  String datasourceName;

  @PathParam("modelName")
  String modelName;

  @Inject
  DataApplicationService dataApplicationService;

  @Operation(summary = "获取模型数据记录列表")
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

  @Operation(summary = "获取单条模型数据记录")
  @GET
  @Path("/{id}")
  public Map<String, Object> findOneRecord(@PathParam("id") String id, @QueryParam("nestedQuery") @DefaultValue("false") boolean nestedQuery) {
    return dataApplicationService.findOneRecord(datasourceName, modelName, id, nestedQuery);
  }

  @Operation(summary = "创建模型数据记录")
  @POST
  public Map<String, Object> createRecord(Map<String, Object> record) {
    return dataApplicationService.createRecord(datasourceName, modelName, record);
  }

  @Operation(summary = "更新模型数据记录")
  @PUT
  @Path("/{id}")
  public Map<String, Object> updateRecord(@PathParam("id") String id, Map<String, Object> record) {
    return dataApplicationService.updateRecord(datasourceName, modelName, id, record);
  }

  @Operation(summary = "删除模型数据记录")
  @DELETE
  @Path("/{id}")
  public void deleteRecord(@PathParam("id") String id) {
    dataApplicationService.deleteRecord(datasourceName, modelName, id);
  }

}
