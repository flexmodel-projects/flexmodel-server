package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Index;
import tech.wetech.flexmodel.Model;
import tech.wetech.flexmodel.TypedField;
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
  public Entity createModel(Entity entity) {
    return modelingApplicationService.createModel(datasourceName, entity);
  }

  @DELETE
  @Path("/{modelName}")
  public void dropModel(@PathParam("modelName") String modelName) {
    modelingApplicationService.dropModel(datasourceName, modelName);
  }

  @POST
  @Path("/{modelName}/fields")
  public TypedField<?, ?> createField(@PathParam("modelName") String modelName, TypedField<?, ?> field) {
    field.setModelName(modelName);
    return modelingApplicationService.createField(datasourceName, field);
  }

  @DELETE
  @Path("/{modelName}/fields/{fieldName}")
  public void dropField(@PathParam("modelName") String modelName, @PathParam("fieldName") String fieldName) {
    modelingApplicationService.dropField(datasourceName, modelName, fieldName);
  }

  @POST
  @Path("/{modelName}/indexes")
  public Index createIndex(@PathParam("modelName") String modelName, Index index) {
    index.setModelName(modelName);
    return modelingApplicationService.createIndex(datasourceName, index);
  }

  @DELETE
  @Path("/{modelName}/indexes/{indexName}")
  public void dropIndex(@PathParam("modelName") String modelName, @PathParam("indexName") String indexName) {
    modelingApplicationService.dropIndex(datasourceName, modelName, indexName);
  }

}
