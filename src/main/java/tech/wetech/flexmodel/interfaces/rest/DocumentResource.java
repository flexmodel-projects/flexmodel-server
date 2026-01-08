package tech.wetech.flexmodel.interfaces.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.DocumentApplicationService;
import tech.wetech.flexmodel.shared.SessionContextHolder;

import java.util.Map;

/**
 * @author cjbi
 */
@Tag(name = "【Flexmodel】接口文档", description = "接口文档管理")
@Path("/f/docs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentResource {

  @Inject
  DocumentApplicationService documentApplicationService;

  @Operation(summary = "获取接口文档")
  @GET
  @Path("/{projectId}/openapi.json")
  @PermitAll
  public Map<String, Object> getOpenApi(@PathParam("projectId") String projectId) {
    SessionContextHolder.setProjectId(projectId);
    return documentApplicationService.getOpenApi(projectId);
  }

}
