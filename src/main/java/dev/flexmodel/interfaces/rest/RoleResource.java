package dev.flexmodel.interfaces.rest;

import dev.flexmodel.application.AuthApplicationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.dto.RoleRequest;
import dev.flexmodel.application.dto.RoleResponse;

import java.util.List;

@Tag(name = "角色", description = "角色管理")
@Path("/v1/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {

  @Inject
  AuthApplicationService authApplicationService;

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        type = SchemaType.ARRAY,
        implementation = RoleResponse.class
      )
    )
    })
  @Operation(summary = "获取角色列表")
  @GET
  public List<RoleResponse> findAll() {
    return authApplicationService.findAllRoles();
  }

  @Parameter(name = "roleId", description = "角色ID", in = ParameterIn.PATH)
  @Operation(summary = "获取角色详情")
  @GET
  @Path("/{roleId}")
  public RoleResponse findById(@PathParam("roleId") String roleId) {
    return authApplicationService.findRoleById(roleId);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = RoleRequest.class
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
        implementation = RoleResponse.class
      )
    )
    })
  @Operation(summary = "创建角色")
  @POST
  public RoleResponse createRole(RoleRequest request) {
    return authApplicationService.createRole(request);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = RoleRequest.class
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
        implementation = RoleResponse.class
      )
    )
    })
  @Parameter(name = "roleId", description = "角色ID", in = ParameterIn.PATH)
  @Operation(summary = "更新角色")
  @PUT
  @Path("/{roleId}")
  public RoleResponse updateRole(@PathParam("roleId") String roleId, RoleRequest request) {
    request.setId(roleId);
    return authApplicationService.updateRole(request);
  }

  @Parameter(name = "roleId", description = "角色ID", in = ParameterIn.PATH)
  @Operation(summary = "删除角色")
  @DELETE
  @Path("/{roleId}")
  public void deleteRole(@PathParam("roleId") String roleId) {
    authApplicationService.deleteRole(roleId);
  }

  @Schema(
    description = "角色请求",
    properties = {
      @SchemaProperty(name = "id", description = "ID"),
      @SchemaProperty(name = "name", description = "角色名称"),
      @SchemaProperty(name = "description", description = "角色描述"),
      @SchemaProperty(name = "resoureceIds", description = "资源ID, 逗号分隔"),
      @SchemaProperty(name = "createdBy", description = "创建人"),
      @SchemaProperty(name = "updatedBy", description = "更新人"),
    }
  )
  public static class RoleRequestSchema extends RoleRequest {

  }
}
