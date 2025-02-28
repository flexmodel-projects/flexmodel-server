package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.FlexmodelConfig;
import tech.wetech.flexmodel.TypeWrapper;
import tech.wetech.flexmodel.application.ModelingApplicationService;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.codegen.enumeration.DatasourceType;
import tech.wetech.flexmodel.domain.model.connect.NativeQueryResult;
import tech.wetech.flexmodel.domain.model.connect.ValidateResult;
import tech.wetech.flexmodel.domain.model.connect.database.Database;
import tech.wetech.flexmodel.util.JsonUtils;

import java.util.*;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "数据源", description = "数据源管理")
@Path(BASE_PATH + "/datasources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DatasourceResource {

  @Inject
  ModelingApplicationService modelingApplicationService;

  @Inject
  FlexmodelConfig config;

  @Operation(summary = "验证数据源连接")
  @POST
  @Path("/validate")
  public ValidateResult validateConnection(Datasource datasource) {
    return modelingApplicationService.validateConnection(datasource);
  }

  @Operation(summary = "从数据源同步物理表到建模")
  @POST
  @Path("/{datasourceName}/sync")
  public List<TypeWrapper> syncModels(@PathParam("datasourceName") String datasourceName, Set<String> models) {
    return modelingApplicationService.syncModels(datasourceName, models);
  }

  @Operation(summary = "导入模型到数据源")
  @POST
  @Path("/{datasourceName}/import")
  public void importModels(@PathParam("datasourceName") String datasourceName, ImportScriptRequest request) {
    modelingApplicationService.importModels(datasourceName, request.script());
  }

  @Operation(summary = "获取物理数据库表名称")
  @POST
  @Path("/physics/names")
  public List<String> getPhysicsModelNames(Datasource datasource) {
    return modelingApplicationService.getPhysicsModelNames(datasource);
  }

  @Operation(summary = "执行原生查询")
  @POST
  @Path("/{datasourceName}/native-query")
  public NativeQueryResult executeNativeQuery(@PathParam("datasourceName") String datasourceName, ExecuteNativeQueryRequest request) {
    return modelingApplicationService.executeNativeQuery(datasourceName, request.statement(), request.parameters());
  }

  @Operation(summary = "获取所有数据源")
  @GET
  public List<Datasource> findAll() {
    List<Datasource> datasourceList = modelingApplicationService.findDatasourceList();
    List<Datasource> allList = new ArrayList<>();
    Datasource system = new Datasource();
    system.setName("system");
    system.setType(DatasourceType.system);
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

  @Operation(summary = "创建数据源")
  @POST
  public Datasource createDatasource(Datasource datasource) {
    return modelingApplicationService.createDatasource(datasource);
  }

  @Operation(summary = "更新数据源")
  @PUT
  @Path("/{datasourceName}")
  public Datasource updateDatasource(@PathParam("datasourceName") String datasourceName, Datasource datasource) {
    datasource.setName(datasourceName);
    return modelingApplicationService.updateDatasource(datasource);
  }

  @Operation(summary = "删除数据源")
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
