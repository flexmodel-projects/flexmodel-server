package dev.flexmodel.interfaces.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.DocumentApplicationService;

import java.util.Map;

/**
 * @author cjbi
 */
@Tag(name = "接口文档", description = "接口文档管理")
@Path("/v1/projects/{projectId}/docs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentResource {

  @Inject
  DocumentApplicationService documentApplicationService;

  @Operation(summary = "获取接口文档")
  @GET
  @Path("/openapi.json")
  @PermitAll
  public Map<String, Object> getOpenApi(@PathParam("projectId") String projectId) {
    return documentApplicationService.getOpenApi(projectId);
  }

}
