package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.application.DocumentApplicationService;

import java.util.Map;

/**
 * @author cjbi
 */
@Path("/api/docs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentResource {

  @Inject
  DocumentApplicationService documentApplicationService;

  @GET
  @Path("/openapi.json")
  public Map<String, Object> getOpenAPI() {
    return documentApplicationService.getOpenAPI();
  }

}
