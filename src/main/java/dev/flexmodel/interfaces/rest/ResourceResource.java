package dev.flexmodel.interfaces.rest;

import dev.flexmodel.application.AuthApplicationService;
import dev.flexmodel.application.dto.ResourceTreeResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.dto.ResourceResponse;

import java.util.List;

@Tag(name = "资源", description = "资源管理")
@Path("/v1/resources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResourceResource {

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
        implementation = ResourceResponse.class
      )
    )
    })
  @Operation(summary = "获取资源列表")
  @GET
  public List<ResourceResponse> findAllResources() {
    return authApplicationService.findAllResources();
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        type = SchemaType.ARRAY,
        implementation = ResourceResponse.class
      )
    )
    })
  @Operation(summary = "获取资源树")
  @GET
  @Path("/tree")
  public List<ResourceTreeResponse> findResourceTree() {
    return authApplicationService.findResourceTree();
  }


}
