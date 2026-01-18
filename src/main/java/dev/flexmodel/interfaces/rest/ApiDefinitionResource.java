package dev.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.ApiDefinitionApplicationService;
import dev.flexmodel.application.dto.ApiDefinitionTreeDTO;
import dev.flexmodel.application.dto.GenerateAPIsDTO;
import dev.flexmodel.codegen.entity.ApiDefinition;
import dev.flexmodel.codegen.entity.ApiDefinitionHistory;

import java.util.List;


/**
 * @author cjbi
 */
@Tag(name = "接口定义", description = "接口定义管理")
@Path("/v1/projects/{projectId}/apis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiDefinitionResource {

  @Inject
  ApiDefinitionApplicationService apiDesignApplicationService;

  @Operation(summary = "获取接口定义列表")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiDefinitionTreeSchema.class
      )
    )})
  @GET
  public List<ApiDefinitionTreeDTO> findApiList(@PathParam("projectId") String projectId) {
    return apiDesignApplicationService.findApiDefinitionTree(projectId);
  }

  @GET
  @Path("/{apiDefinitionId}/histories")
  public List<ApiDefinitionHistory> findApiDefinitionHistories(@PathParam("projectId") String projectId, @PathParam("apiDefinitionId") String apiDefinitionId) {
    return apiDesignApplicationService.findApiDefinitionHistories(projectId, apiDefinitionId);
  }

  @Operation(summary = "恢复接口定义")
  @POST
  @Path("/{apiDefinitionId}/histories/{historyId}/restore")
  public ApiDefinitionHistory restoreApiDefinition(@PathParam("projectId") String projectId, @PathParam("historyId") String historyId, @PathParam("apiDefinitionId") String apiDefinitionId) {
    return apiDesignApplicationService.restoreApiDefinition(projectId, historyId);
  }

  @Operation(summary = "创建接口定义")
  @POST
  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiDefinitionSchema.class
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
        implementation = ApiDefinitionSchema.class
      )
    )})
  public ApiDefinition create(@PathParam("projectId") String projectId, ApiDefinition apiDefinition) {
    apiDefinition.setProjectId(projectId);
    return apiDesignApplicationService.createApiDefinition(projectId, apiDefinition);
  }

  @Operation(summary = "更新接口定义")
  @PUT
  @Path("/{id}")
  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiDefinitionSchema.class
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
        implementation = ApiDefinitionSchema.class
      )
    )})
  public ApiDefinition update(@PathParam("projectId") String projectId, @PathParam("id") String id, ApiDefinition apiDefinition) {
    apiDefinition.setId(id);
    apiDefinition.setProjectId(projectId);
    return apiDesignApplicationService.updateApiDefinition(projectId, apiDefinition);
  }

  @Operation(summary = "更新接口定义(局部更新)")
  @PATCH
  @Path("/{id}")
  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiDefinitionSchema.class
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
        implementation = ApiDefinitionSchema.class
      )
    )})
  public ApiDefinition updateIgnoreNull(@PathParam("projectId") String projectId, @PathParam("id") String id, ApiDefinition request) {
    request.setId(id);
    ApiDefinition record = apiDesignApplicationService.findApiDefinition(projectId, id);
    if (request.getName() != null) {
      record.setName(request.getName());
    }
    if (request.getEnabled() != null) {
      record.setEnabled(request.getEnabled());
    }
    apiDesignApplicationService.updateApiDefinition(projectId, record);
    return record;
  }

  @Operation(summary = "删除接口定义")
  @DELETE
  @Path("/{id}")
  public void delete(@PathParam("projectId") String projectId, @PathParam("id") String id) {
    apiDesignApplicationService.deleteApiDefinition(projectId, id);
  }

  @Operation(summary = "根据模型生成接口定义")
  @POST
  @Path("/generate")
  public void generateAPIs(@PathParam("projectId") String projectId, GenerateAPIsDTO dto) {
    apiDesignApplicationService.generateAPIs(projectId, dto);
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "id", examples = {"-1"}, description = "唯一标识"),
      @SchemaProperty(name = "type", description = "类型, FOLDER: 文件夹；API: 接口"),
      @SchemaProperty(name = "path", description = "路径"),
      @SchemaProperty(name = "meta", description = "元数据，存放接口定义，例如graphql定义"),
      @SchemaProperty(name = "name", description = "名称"),
      @SchemaProperty(name = "createdAt", description = "创建时间", readOnly = true),
      @SchemaProperty(name = "updatedAt", description = "更新时间", readOnly = true),
      @SchemaProperty(name = "parentId", description = "上级ID"),
      @SchemaProperty(name = "enabled", description = "是否开启", defaultValue = "true"),
      @SchemaProperty(name = "method", description = "HTTP请求方法", examples = {"GET"}),
      @SchemaProperty(name = "children", description = "子节点", type = SchemaType.ARRAY),
      @SchemaProperty(name = "projectId", description = "项目ID", readOnly = true)
    }
  )
  public static class ApiDefinitionTreeSchema extends ApiDefinitionTreeDTO {

  }

  @Schema(
    properties = {
      @SchemaProperty(name = "id", examples = {"-1"}, description = "唯一标识"),
      @SchemaProperty(name = "type", description = "类型, FOLDER: 文件夹；API: 接口"),
      @SchemaProperty(name = "path", description = "路径"),
      @SchemaProperty(name = "meta", description = "元数据，存放接口定义，例如graphql定义"),
      @SchemaProperty(name = "name", description = "名称"),
      @SchemaProperty(name = "createdAt", description = "创建时间", readOnly = true),
      @SchemaProperty(name = "updatedAt", description = "更新时间", readOnly = true),
      @SchemaProperty(name = "parentId", description = "上级ID"),
      @SchemaProperty(name = "enabled", description = "是否开启", defaultValue = "true"),
      @SchemaProperty(name = "method", description = "HTTP请求方法", examples = {"GET"}),
      @SchemaProperty(name = "projectId", description = "项目ID", readOnly = true)
    }
  )
  public static class ApiDefinitionSchema extends ApiDefinition {

  }

}
