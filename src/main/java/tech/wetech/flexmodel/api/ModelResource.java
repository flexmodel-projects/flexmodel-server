package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Index;
import tech.wetech.flexmodel.TypeWrapper;
import tech.wetech.flexmodel.TypedField;
import tech.wetech.flexmodel.application.ModelingApplicationService;

import java.util.List;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "模型", description = "模型管理")
@Path(BASE_PATH + "/datasources/{datasourceName}/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

  @PathParam("datasourceName")
  String datasourceName;

  @Inject
  ModelingApplicationService modelingApplicationService;

  @Operation(summary = "获取模型列表")
  @GET
  public List<TypeWrapper> findModels() {
    return modelingApplicationService.findModels(datasourceName);
  }

  @Operation(summary = "创建模型")
  @Parameters(value = {@Parameter(description = "实体", schema = @Schema(implementation = Entity.class))})
  @POST
  public TypeWrapper createModel(
    TypeWrapper model) {
    return modelingApplicationService.createModel(datasourceName, model);
  }

  @Operation(summary = "更新模型")
  @PUT
  @Path("/{modelName}")
  public TypeWrapper modifyModel(@PathParam("modelName") String modelName, TypeWrapper model) {
    return modelingApplicationService.modifyModel(datasourceName, modelName, model);
  }

  @Operation(summary = "删除模型")
  @DELETE
  @Path("/{modelName}")
  public void dropModel(@PathParam("modelName") String modelName) {
    modelingApplicationService.dropModel(datasourceName, modelName);
  }

  @Operation(summary = "创建字段")
  @POST
  @Path("/{modelName}/fields")
  public TypedField<?, ?> createField(@PathParam("modelName") String modelName, TypedField<?, ?> field) {
    field.setModelName(modelName);
    return modelingApplicationService.createField(datasourceName, field);
  }

  @Operation(summary = "更新字段")
  @PUT
  @Path("/{modelName}/fields/{fieldName}")
  public TypedField<?, ?> modifyField(@PathParam("modelName") String modelName, @PathParam("fieldName") String fieldName, TypedField<?, ?> field) {
    field.setModelName(modelName);
    field.setName(fieldName);
    return modelingApplicationService.modifyField(datasourceName, field);
  }

  @Operation(summary = "删除字段")
  @DELETE
  @Path("/{modelName}/fields/{fieldName}")
  public void dropField(@PathParam("modelName") String modelName, @PathParam("fieldName") String fieldName) {
    modelingApplicationService.dropField(datasourceName, modelName, fieldName);
  }

  @Operation(summary = "创建索引")
  @POST
  @Path("/{modelName}/indexes")
  public Index createIndex(@PathParam("modelName") String modelName, Index index) {
    index.setModelName(modelName);
    return modelingApplicationService.createIndex(datasourceName, index);
  }

  @Operation(summary = "更新索引")
  @PUT
  @Path("/{modelName}/indexes/{indexName}")
  public Index modifyIndex(@PathParam("modelName") String modelName, @PathParam("indexName") String indexName, Index index) {
    index.setModelName(modelName);
    index.setName(indexName);
    return modelingApplicationService.modifyIndex(datasourceName, index);
  }

  @Operation(summary = "删除索引")
  @DELETE
  @Path("/{modelName}/indexes/{indexName}")
  public void dropIndex(@PathParam("modelName") String modelName, @PathParam("indexName") String indexName) {
    modelingApplicationService.dropIndex(datasourceName, modelName, indexName);
  }

}
