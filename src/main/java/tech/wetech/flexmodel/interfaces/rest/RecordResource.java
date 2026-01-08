package tech.wetech.flexmodel.interfaces.rest;

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
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.DataApplicationService;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.shared.SessionContextHolder;

import java.util.Map;

/**
 * @author cjbi
 */
@Tag(name = "【Flexmodel】记录", description = "模型数据记录管理")
@Path("/f/datasources/{datasourceName}/models/{modelName}/records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecordResource {

  @Parameter(name = "datasourceName", description = "数据源名称", in = ParameterIn.PATH)
  @PathParam("datasourceName")
  String datasourceName;

  @Parameter(name = "datasourceName", description = "模型名称", in = ParameterIn.PATH)
  @PathParam("modelName")
  String modelName;

  @Inject
  DataApplicationService dataApplicationService;


  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        properties = {
          @SchemaProperty(name = "total", description = "总数"),
          @SchemaProperty(name = "list", description = "日志列表", type = SchemaType.ARRAY, implementation = Map.class)
        }
      )
    )
    })
  @Parameter(name = "page", description = "当前页，默认值：1", examples = {@ExampleObject(value = "1")}, in = ParameterIn.QUERY)
  @Parameter(name = "size", description = "第几页，默认值：15", examples = {@ExampleObject(value = "15")}, in = ParameterIn.QUERY)
  @Parameter(
    name = "filter", description = "查询条件，更多信息见查询条件文档",
    examples = {@ExampleObject(value = """
      "{ \\"username\\": { \\"_eq\\": \\"john_doe\\" } }"
      """)},
    in = ParameterIn.QUERY)
  @Parameter(name = "nestedQuery", description = "是否开启嵌套子查询，开启则查询关联数据，只查询5层，默认值false", examples = {@ExampleObject(value = "false")}, in = ParameterIn.QUERY)
  @Parameter(name = "sort", description = "排序", examples = {@ExampleObject(value = """
    "[{\\"field\\":\\"name\\",\\"sort\\":\\"ASC\\"}, {\\"field\\":\\"id\\",\\"sort\\":\\"DESC\\"}]"
    """)}, in = ParameterIn.QUERY)
  @Operation(summary = "获取模型数据记录列表")
  @GET
  public PageDTO<Map<String, Object>> findPagingRecords(
    @QueryParam("page") @DefaultValue("1") int page,
    @QueryParam("size") @DefaultValue("15") int size,
    @QueryParam("filter") String filter,
    @QueryParam("nestedQuery") @DefaultValue("false") boolean nestedQuery,
    @QueryParam("sort") String sort
  ) {
    SessionContextHolder.setProjectId(null);
    return dataApplicationService.findPagingRecords(datasourceName, modelName, page, size, filter, sort, nestedQuery);
  }

  @Parameter(name = "id", description = "ID", examples = {@ExampleObject(value = "1")}, in = ParameterIn.PATH)
  @Operation(summary = "获取单条模型数据记录")
  @GET
  @Path("/{id}")
  public Map<String, Object> findOneRecord(@PathParam("id") String id, @QueryParam("nestedQuery") @DefaultValue("false") boolean nestedQuery) {
    return dataApplicationService.findOneRecord(datasourceName, modelName, id, nestedQuery);
  }


  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      examples = {
        @ExampleObject(name = "请求示例，可包含关联数据，关联数据有Id字段则更新这条关联记录", value = """
            {
            "studentName": "张三",
            "gender": "MALE",
            "interest": ["chang", "tiao", "rap", "daLanQiu"],
            "age": 10,
            "classId": 1,
            "studentDetail": {
              "description": "张三的描述"
            },
            "courses": [
               {
                 "courseNo":"Math",
                 "courseName":"数学"
               },
               {
                 "courseNo":"YuWen",
                 "courseName":"语文"
               },
               {
                 "courseNo":"Eng",
                 "courseName":"英语"
               }
            ]
          }
          """)
      }
    )}
  )
  @Operation(summary = "创建模型数据记录")
  @POST
  public Map<String, Object> createRecord(Map<String, Object> record) {
    return dataApplicationService.createRecord(datasourceName, modelName, record);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      examples = {
        @ExampleObject(name = "请求示例，可包含关联数据，关联数据有Id字段则更新这条关联记录", value = """
            {
            "id": 1,
            "studentName": "张三",
            "gender": "MALE",
            "interest": ["chang", "tiao", "rap", "daLanQiu"],
            "age": 10,
            "classId": 1,
            "studentDetail": {
              "description": "张三的描述"
            },
            "courses": [
               {
                 "courseNo":"Math",
                 "courseName":"数学"
               },
               {
                 "courseNo":"YuWen",
                 "courseName":"语文"
               },
               {
                 "courseNo":"Eng",
                 "courseName":"英语"
               }
            ]
          }
          """)
      }
    )}
  )
  @Parameter(name = "id", description = "ID", examples = {@ExampleObject(value = "1")}, in = ParameterIn.PATH)
  @Operation(summary = "更新模型数据记录")
  @PUT
  @Path("/{id}")
  public Map<String, Object> updateRecord(@PathParam("id") String id, Map<String, Object> record) {
    return dataApplicationService.updateRecord(datasourceName, modelName, id, record);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      examples = {
        @ExampleObject(name = "请求示例，可包含关联数据，关联数据有Id字段则更新这条关联记录", value = """
            {
            "studentName": "张三",
            "gender": "MALE",
            "interest": ["chang", "tiao", "rap", "daLanQiu"],
            "age": 10,
            "classId": 1,
            "studentDetail": {
              "description": "张三的描述"
            },
            "courses": [
               {
                 "courseNo":"Math",
                 "courseName":"数学"
               },
               {
                 "courseNo":"YuWen",
                 "courseName":"语文"
               },
               {
                 "courseNo":"Eng",
                 "courseName":"英语"
               }
            ]
          }
          """)
      }
    )}
  )
  @Parameter(name = "id", description = "ID", examples = {@ExampleObject(value = "1")}, in = ParameterIn.PATH)
  @Operation(summary = "更新模型数据记录(局部更新)")
  @PATCH
  @Path("/{id}")
  public Map<String, Object> updateRecordIgnoreNull(@PathParam("id") String id, Map<String, Object> record) {
    return dataApplicationService.updateRecordIgnoreNull(datasourceName, modelName, id, record);
  }

  @Parameter(name = "id", description = "ID", examples = {@ExampleObject(value = "1")}, in = ParameterIn.PATH)
  @Operation(summary = "删除模型数据记录")
  @DELETE
  @Path("/{id}")
  public void deleteRecord(@PathParam("id") String id) {
    dataApplicationService.deleteRecord(datasourceName, modelName, id);
  }

}
