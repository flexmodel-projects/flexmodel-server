package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.FlexmodelConfig;
import tech.wetech.flexmodel.TypeWrapper;
import tech.wetech.flexmodel.application.ModelingApplicationService;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.domain.model.connect.NativeQueryResult;
import tech.wetech.flexmodel.domain.model.connect.ValidateResult;
import tech.wetech.flexmodel.domain.model.connect.database.Database;
import tech.wetech.flexmodel.util.JsonUtils;

import java.util.*;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Path(BASE_PATH + "/datasources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DatasourceResource {

  @Inject
  ModelingApplicationService modelingApplicationService;

  @Inject
  FlexmodelConfig config;

  @POST
  @Path("/validate")
  public ValidateResult validateConnection(Datasource datasource) {
    return modelingApplicationService.validateConnection(datasource);
  }

  @POST
  @Path("/{datasourceName}/sync")
  public List<TypeWrapper> syncModels(@PathParam("datasourceName") String datasourceName, Set<String> models) {
    return modelingApplicationService.syncModels(datasourceName, models);
  }

  @POST
  @Path("/{datasourceName}/import")
  public void importModels(@PathParam("datasourceName") String datasourceName, @Valid ImportScriptRequest request) {
    modelingApplicationService.importModels(datasourceName, request.script());
  }

  @POST
  @Path("/physics/names")
  public List<String> getPhysicsModelNames(Datasource datasource) {
    return modelingApplicationService.getPhysicsModelNames(datasource);
  }

  @POST
  @Path("/{datasourceName}/native-query")
  public NativeQueryResult executeNativeQuery(@PathParam("datasourceName") String datasourceName, ExecuteNativeQueryRequest request) {
    return modelingApplicationService.executeNativeQuery(datasourceName, request.statement(), request.parameters());
  }

  @GET
  public List<Datasource> findAll() {
    List<Datasource> datasourceList = modelingApplicationService.findDatasourceList();
    List<Datasource> allList = new ArrayList<>();
    Datasource system = new Datasource();
    system.setName("system");
    system.setType("system");
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("url", config.datasource().url());
    configMap.put("dbKind", config.datasource().dbKind());
    configMap.put("username", config.datasource().username());
    configMap.put("password", config.datasource().password());

    system.setConfig(JsonUtils.getInstance().convertValue(configMap, Database.class));
    allList.add(system);
    allList.addAll(datasourceList);
    return allList;
  }

  @POST
  public Datasource createDatasource(@Valid Datasource datasource) {
    return modelingApplicationService.createDatasource(datasource);
  }

  @PUT
  @Path("/{datasourceName}")
  public Datasource updateDatasource(@PathParam("datasourceName") String datasourceName, @Valid Datasource datasource) {
    datasource.setName(datasourceName);
    return modelingApplicationService.updateDatasource(datasource);
  }

  @DELETE
  @Path("/{datasourceName}")
  public void deleteDatasource(@PathParam("datasourceName") String datasourceName) {
    modelingApplicationService.deleteDatasource(datasourceName);
  }

  public record ImportScriptRequest(@NotBlank String script) {

  }

  public record ExecuteNativeQueryRequest(String statement, Map<String, Object> parameters) {
  }

}
