package tech.wetech.flexmodel.api;

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
import tech.wetech.flexmodel.application.ApiDesignApplicationService;
import tech.wetech.flexmodel.application.dto.ApiInfoTreeDTO;
import tech.wetech.flexmodel.application.dto.GenerateAPIsDTO;
import tech.wetech.flexmodel.codegen.entity.ApiInfo;

import java.util.List;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "接口信息", description = "接口信息管理")
@Path(BASE_PATH + "/apis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiInfoResource {

  @Inject
  ApiDesignApplicationService apiDesignApplicationService;

  @Operation(summary = "获取接口信息列表")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "成功",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiInfoTreeSchema.class
      )
    )})
  @GET
  public List<ApiInfoTreeDTO> findApiList() {
    return apiDesignApplicationService.findApiInfoTree();
  }

  @Operation(summary = "创建接口信息")
  @POST
  @RequestBody(
    name = "请求参数",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiInfoSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "成功",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiInfoSchema.class
      )
    )})
  public ApiInfo create(ApiInfo apiInfo) {
    return apiDesignApplicationService.createApiInfo(apiInfo);
  }

  @Operation(summary = "更新接口信息")
  @PUT
  @Path("/{id}")
  @RequestBody(
    name = "请求参数",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiInfoSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "成功",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiInfoSchema.class
      )
    )})
  public ApiInfo update(@PathParam("id") String id, ApiInfo apiInfo) {
    apiInfo.setId(id);
    return apiDesignApplicationService.updateApiInfo(apiInfo);
  }

  @Operation(summary = "更新接口信息(局部更新)")
  @PATCH
  @Path("/{id}")
  @RequestBody(
    name = "请求参数",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiInfoSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "成功",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ApiInfoSchema.class
      )
    )})
  public ApiInfo updateIgnoreNull(@PathParam("id") String id, ApiInfo apiInfo) {
    apiInfo.setId(id);
    return apiDesignApplicationService.updateApiInfoIgnoreNull(apiInfo);
  }

  @Operation(summary = "删除接口信息")
  @DELETE
  @Path("/{id}")
  public void delete(@PathParam("id") String id) {
    apiDesignApplicationService.deleteApiInfo(id);
  }

  @Operation(summary = "根据模型生成接口信息")
  @POST
  @Path("/generate")
  public void generateAPIs(GenerateAPIsDTO dto) {
    apiDesignApplicationService.generateAPIs(dto);
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "children", description = "子节点", type = SchemaType.ARRAY)
    }
  )
  public static class ApiInfoTreeSchema extends ApiInfoSchema {

  }

  @Schema(
    properties = {
      @SchemaProperty(name = "id", example = "-1", description = "唯一标识"),
      @SchemaProperty(name = "type", description = "类型, FOLDER: 文件夹；API: 接口"),
      @SchemaProperty(name = "path", description = "路径"),
      @SchemaProperty(name = "meta", description = "元数据，存放接口定义，例如graphql定义"),
      @SchemaProperty(name = "name", description = "名称"),
      @SchemaProperty(name = "createdAt", description = "创建时间"),
      @SchemaProperty(name = "updatedAt", description = "更新时间"),
      @SchemaProperty(name = "parentId", description = "上级ID"),
      @SchemaProperty(name = "enabled", description = "是否开启", defaultValue = "true"),
      @SchemaProperty(name = "method", description = "HTTP请求方法", example = "GET"),
    }
  )
  public static class ApiInfoSchema extends ApiInfo {

  }

}
