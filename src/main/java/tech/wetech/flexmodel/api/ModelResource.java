package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Model;
import tech.wetech.flexmodel.application.ModelingApplicationService;

import java.util.List;

/**
 * @author cjbi
 */
@Path("/api/datasources/{datasourceName}/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

  @PathParam("datasourceName")
  String datasourceName;

  @Inject
  ModelingApplicationService modelingApplicationService;

  @GET
  public List<Model> findModels() {
   return modelingApplicationService.findModels(datasourceName);
  }

  @POST
  public Entity createEntity(Entity entity) {
    return entity;
  }


}
