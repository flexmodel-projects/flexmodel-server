package tech.wetech.flexmodel.interfaces.rest;

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
import tech.wetech.flexmodel.application.MemberApplicationService;
import tech.wetech.flexmodel.application.dto.MemberRequest;
import tech.wetech.flexmodel.application.dto.MemberResponse;

import java.util.List;

/**
 * @author cjbi
 */
@Tag(name = "成员", description = "成员管理")
@Path("/v1/members")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemberResource {

  @Inject
  MemberApplicationService memberApplicationService;

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        type = SchemaType.ARRAY,
        implementation = MemberResponse.class
      )
    )
    })
  @Operation(summary = "获取成员列表")
  @GET
  public List<MemberResponse> findAll() {
    return memberApplicationService.findAll();
  }

  @Parameter(name = "userId", description = "成员ID", in = ParameterIn.PATH)
  @Operation(summary = "获取成员详情")
  @GET
  @Path("/{userId}")
  public MemberResponse findById(@PathParam("userId") String userId) {
    return memberApplicationService.findById(userId);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = MemberRequest.class
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
        implementation = MemberResponse.class
      )
    )
    })
  @Operation(summary = "创建成员")
  @POST
  public MemberResponse createMember(MemberRequest request) {
    return memberApplicationService.createMember(request);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = MemberRequest.class
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
        implementation = MemberResponse.class
      )
    )
    })
  @Parameter(name = "userId", description = "成员ID", in = ParameterIn.PATH)
  @Operation(summary = "更新成员")
  @PUT
  @Path("/{userId}")
  public MemberResponse updateMember(@PathParam("userId") String userId, MemberRequest request) {
    request.setId(userId);
    return memberApplicationService.updateMember(request);
  }

  @Parameter(name = "userId", description = "成员ID", in = ParameterIn.PATH)
  @Operation(summary = "删除成员")
  @DELETE
  @Path("/{userId}")
  public void deleteMember(@PathParam("userId") String userId) {
    memberApplicationService.deleteMember(userId);
  }

  @Schema(
    description = "成员请求",
    properties = {
      @SchemaProperty(name = "id", description = "ID"),
      @SchemaProperty(name = "username", description = "用户名"),
      @SchemaProperty(name = "email", description = "邮箱"),
      @SchemaProperty(name = "password", description = "密码（明文，创建或更新时可选）"),
      @SchemaProperty(name = "createdBy", description = "创建人"),
      @SchemaProperty(name = "updatedBy", description = "更新人"),
    }
  )
  public static class MemberRequestSchema extends MemberRequest {

  }
}
