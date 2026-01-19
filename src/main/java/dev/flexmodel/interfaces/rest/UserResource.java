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
import dev.flexmodel.application.dto.UserRequest;
import dev.flexmodel.application.dto.UserResponse;

import java.util.List;

/**
 * @author cjbi
 */
@Tag(name = "用户", description = "用户管理")
@Path("/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

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
        implementation = UserResponse.class
      )
    )
    })
  @Operation(summary = "获取用户列表")
  @GET
  public List<UserResponse> findAll() {
    return authApplicationService.findAllUsers();
  }

  @Parameter(name = "userId", description = "用户ID", in = ParameterIn.PATH)
  @Operation(summary = "获取用户详情")
  @GET
  @Path("/{userId}")
  public UserResponse findById(@PathParam("userId") String userId) {
    return authApplicationService.findUserById(userId);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = UserRequest.class
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
        implementation = UserResponse.class
      )
    )
    })
  @Operation(summary = "创建用户")
  @POST
  public UserResponse createUser(UserRequest request) {
    return authApplicationService.createUser(request);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = UserRequest.class
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
        implementation = UserResponse.class
      )
    )
    })
  @Parameter(name = "userId", description = "用户ID", in = ParameterIn.PATH)
  @Operation(summary = "更新用户")
  @PUT
  @Path("/{userId}")
  public UserResponse updateUser(@PathParam("userId") String userId, UserRequest request) {
    request.setId(userId);
    return authApplicationService.updateUser(request);
  }

  @Parameter(name = "userId", description = "用户ID", in = ParameterIn.PATH)
  @Operation(summary = "删除用户")
  @DELETE
  @Path("/{userId}")
  public void deleteUser(@PathParam("userId") String userId) {
    authApplicationService.deleteUser(userId);
  }

  @Schema(
    description = "用户请求",
    properties = {
      @SchemaProperty(name = "id", description = "ID"),
      @SchemaProperty(name = "name", description = "用户名"),
      @SchemaProperty(name = "email", description = "邮箱"),
      @SchemaProperty(name = "password", description = "密码（明文，创建或更新时可选）"),
      @SchemaProperty(name = "roleIds", description = "角色ID列表"),
      @SchemaProperty(name = "createdBy", description = "创建人"),
      @SchemaProperty(name = "updatedBy", description = "更新人"),
    }
  )
  public static class UserRequestSchema extends UserRequest {

  }
}
