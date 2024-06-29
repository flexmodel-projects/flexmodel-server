package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.FlexmodelConfig;
import tech.wetech.flexmodel.application.ModelingApplicationService;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.domain.model.connect.ValidateResult;
import tech.wetech.flexmodel.util.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@Path("/api/datasources")
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

  @GET
  @Path("/{datasourceName}/refresh")
  public List<Entity> refresh(@PathParam("datasourceName") String datasourceName) {
    return modelingApplicationService.refresh(datasourceName);
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

    system.setConfig(JsonUtils.getInstance().convertValue(configMap, Datasource.Database.class));
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

}
