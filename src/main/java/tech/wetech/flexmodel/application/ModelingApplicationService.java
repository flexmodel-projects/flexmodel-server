package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Index;
import tech.wetech.flexmodel.TypedField;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.domain.model.connect.ValidateResult;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;

import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ModelingApplicationService {

  @Inject
  DatasourceService datasourceService;

  @Inject
  ModelService modelService;

  public List<Datasource> findDatasourceList() {
    return datasourceService.findAll();
  }

  public Datasource createDatasource(Datasource datasource) {
    return datasourceService.createDatasource(datasource);
  }

  public Datasource updateDatasource(Datasource record) {
    return datasourceService.updateDatasource(record);
  }

  public void deleteDatasource(String datasourceName) {
    datasourceService.deleteDatasource(datasourceName);
  }

  public List<Entity> findModels(String datasourceName) {
    return modelService.findModels(datasourceName);
  }

  public Entity createModel(String datasourceName, Entity entity) {
    return modelService.createModel(datasourceName, entity);
  }

  public void dropModel(String datasourceName, String modelName) {
    modelService.dropModel(datasourceName, modelName);
  }

  public TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field) {
    return modelService.createField(datasourceName, field);
  }

  public TypedField<?, ?> modifyField(String datasourceName, TypedField<?, ?> field) {
    return modelService.modifyField(datasourceName, field);
  }

  public void dropField(String datasourceName, String modelName, String fieldName) {
    modelService.dropField(datasourceName, modelName, fieldName);
  }

  public Index createIndex(String datasourceName, Index index) {
    return modelService.createIndex(datasourceName, index);
  }

  public Index modifyIndex(String datasourceName, Index index) {
    return modelService.modifyIndex(datasourceName, index);
  }

  public void dropIndex(String datasourceName, String modelName, String indexName) {
    modelService.dropIndex(datasourceName, modelName, indexName);
  }

  public ValidateResult validateConnection(Datasource datasource) {
    return datasourceService.validate(datasource);
  }

  public List<String> getPhysicsModelNames(Datasource datasource) {
    return datasourceService.getPhysicsModelNames(datasource);
  }

  public List<Entity> syncModels(String datasourceName, Set<String> models) {
    return modelService.syncModels(datasourceName, models);
  }

  public void importModels(String datasourceName, String script) {
    modelService.importModels(datasourceName, script);
  }
}
