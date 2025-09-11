package tech.wetech.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.FlowApplicationService;

/**
 * @author cjbi
 */
@Tag(name = "【Flexmodel】服务编排", description = "服务编排管理")
@Path("/f/flow")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FlowResource {

  @Inject
  FlowApplicationService flowApplicationService;

  @Path("/deploy")
  public void deployFlow() {

  }

}
