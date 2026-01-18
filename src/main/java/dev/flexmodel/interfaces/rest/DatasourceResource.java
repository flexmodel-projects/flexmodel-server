package dev.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
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
import dev.flexmodel.application.ModelingApplicationService;
import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.domain.model.connect.NativeQueryResult;
import dev.flexmodel.domain.model.connect.ValidateResult;
import dev.flexmodel.model.SchemaObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author cjbi
 */
@Tag(name = "数据源", description = "数据源管理")
@Path("/v1/projects/{projectId}/datasources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DatasourceResource {

  @Inject
  ModelingApplicationService modelingApplicationService;

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = DatasourceSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        properties = {
          @SchemaProperty(name = "success", description = "是否成功"),
          @SchemaProperty(name = "errorMsg", description = "错误消息"),
          @SchemaProperty(name = "time", description = "耗时")
        }
      )
    )
    })
  @Operation(summary = "验证数据源连接")
  @POST
  @Path("/validate")
  public ValidateResult validateConnection(@PathParam("projectId") String projectId, Datasource datasource) {
    return modelingApplicationService.validateConnection(projectId, datasource);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          type = SchemaType.ARRAY,
          implementation = ModelingResource.EntitySchema.class
        ),
        examples = {
          @ExampleObject(
            name = "实体",
            value = """
              [{ "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "Long","identity": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentName", "type": "String", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "EnumRef", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "EnumRef", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "Int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "Long", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "Relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }]
              """
          )
        }
      )
    })
  @Parameter(name = "datasourceName", description = "数据源名称", in = ParameterIn.PATH)
  @Operation(summary = "从数据源同步物理表到建模")
  @POST
  @Path("/{datasourceName}/sync")
  public List<SchemaObject> syncModels(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, Set<String> models) {
    return modelingApplicationService.syncModels(projectId, datasourceName, models);
  }

  @Parameter(name = "datasourceName", description = "数据源名称", in = ParameterIn.PATH)
  @Operation(summary = "导入模型到数据源")
  @POST
  @Path("/{datasourceName}/import")
  public void importModels(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, ImportScriptRequest request) {
    ImportScriptType type = request.type();
    if (type == null) {
      type = ImportScriptType.JSON;
    }
    modelingApplicationService.importModels(projectId, datasourceName, request.script(), type.name());
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = DatasourceSchema.class
      )
    )}
  )
  @Operation(summary = "获取物理数据库表名称")
  @POST
  @Path("/physics/names")
  public List<String> getPhysicsModelNames(Datasource datasource) {
    return modelingApplicationService.getPhysicsModelNames(datasource);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        properties = {
          @SchemaProperty(name = "time", description = "执行耗时"),
          @SchemaProperty(name = "result", description = "返回结果")
        }
      )
    )
    })
  @Parameter(name = "datasourceName", description = "数据源名称", in = ParameterIn.PATH)
  @Operation(summary = "执行原生查询")
  @POST
  @Path("/{datasourceName}/native-query")
  public NativeQueryResult executeNativeQuery(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, ExecuteNativeQueryRequest request) {
    return modelingApplicationService.executeNativeQuery(projectId, datasourceName, request.statement(), request.parameters());
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        type = SchemaType.ARRAY,
        implementation = DatasourceSchema.class
      )
    )
    })
  @Operation(summary = "获取所有数据源")
  @GET
  public List<Datasource> findAll(@PathParam("projectId") String projectId) {
    return modelingApplicationService.findDatasourceList(projectId);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = DatasourceSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = DatasourceSchema.class
      )
    )
    })
  @Operation(summary = "创建数据源")
  @POST
  public Datasource createDatasource(@PathParam("projectId") String projectId, Datasource datasource) {
    return modelingApplicationService.createDatasource(projectId, datasource);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = DatasourceSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = DatasourceSchema.class
      )
    )
    })
  @Parameter(name = "datasourceName", description = "数据源名称", in = ParameterIn.PATH)
  @Operation(summary = "更新数据源")
  @PUT
  @Path("/{datasourceName}")
  public Datasource updateDatasource(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, Datasource datasource) {
    datasource.setName(datasourceName);
    datasource.setProjectId(projectId);
    return modelingApplicationService.updateDatasource(projectId, datasource);
  }

  @Parameter(name = "datasourceName", description = "数据源名称", in = ParameterIn.PATH)
  @Operation(summary = "删除数据源")
  @DELETE
  @Path("/{datasourceName}")
  public void deleteDatasource(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName) {
    modelingApplicationService.deleteDatasource(projectId, datasourceName);
  }

  public record ImportScriptRequest(@NotBlank @Schema(description = "脚本") String script,
                                    @Schema(description = "脚本类型") ImportScriptType type) {
  }

  public enum ImportScriptType {
    @Schema(description = "JSON")
    JSON,
    @Schema(description = "IDL")
    IDL
  }

  public record ExecuteNativeQueryRequest(@Schema(description = "语句") String statement,
                                          @Schema(description = "参数") Map<String, Object> parameters) {
  }

  @Schema(
    description = "接口日志",
    properties = {
      @SchemaProperty(name = "name", description = "名称，需要唯一"),
      @SchemaProperty(name = "type", description = "数据源类型"),
      @SchemaProperty(name = "config", description = "数据源配置", type = SchemaType.OBJECT),
      @SchemaProperty(name = "enabled", description = "是否启用"),
      @SchemaProperty(name = "createdAt", description = "创建日期", readOnly = true),
      @SchemaProperty(name = "updatedAt", description = "更新日期", readOnly = true),
    }
  )
  public static class DatasourceSchema extends Datasource {

  }

}
