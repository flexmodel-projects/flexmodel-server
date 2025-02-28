package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.DocumentApplicationService;

import java.util.Map;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "接口文档", description = "接口文档管理")
@Path(BASE_PATH + "/docs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentResource {

  @Inject
  DocumentApplicationService documentApplicationService;

  @Operation(summary = "获取接口文档")
  @GET
  @Path("/openapi.json")
  public Map<String, Object> getOpenApi() {
    return documentApplicationService.getOpenApi();
  }

}
