package tech.wetech.flexmodel.interfaces.rest;

import jakarta.annotation.security.PermitAll;
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

import static tech.wetech.flexmodel.interfaces.rest.Resources.ROOT_PATH;

/**
 * @author cjbi
 */
@Tag(name = "【Flexmodel】接口文档", description = "接口文档管理")
@Path(ROOT_PATH + "/docs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentResource {

  @Inject
  DocumentApplicationService documentApplicationService;

  @Operation(summary = "获取接口文档")
  @GET
  @Path("/openapi.json")
  @PermitAll
  public Map<String, Object> getOpenApi() {
    return documentApplicationService.getOpenApi();
  }

}
