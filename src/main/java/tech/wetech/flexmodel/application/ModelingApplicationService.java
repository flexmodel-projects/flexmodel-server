package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.domain.model.connect.NativeQueryResult;
import tech.wetech.flexmodel.domain.model.connect.ValidateResult;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;

import java.util.List;
import java.util.Map;
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

  public List<TypeWrapper> findModels(String datasourceName) {
    return modelService.findModels(datasourceName);
  }

  public TypeWrapper createModel(String datasourceName, TypeWrapper model) {
    return modelService.createModel(datasourceName, model);
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

  public List<TypeWrapper> syncModels(String datasourceName, Set<String> models) {
    return modelService.syncModels(datasourceName, models);
  }

  public void importModels(String datasourceName, String script) {
    modelService.importModels(datasourceName, script);
  }

  public NativeQueryResult executeNativeQuery(String datasourceName, String statement, Map<String, Object> parameters) {
    return datasourceService.executeNativeQuery(datasourceName, statement, parameters);
  }

  public TypeWrapper modifyModel(String datasourceName, String modelName, TypeWrapper model) {
    if (model instanceof Entity) {
      throw new RuntimeException("Unsupported model type");
    }
    if (model instanceof NativeQueryModel nativeQueryModel) {
      nativeQueryModel.setName(modelName);
    }
    if (model instanceof Enum anEnum) {
      anEnum.setName(modelName);
    }
    modelService.dropModel(datasourceName, modelName);
    modelService.createModel(datasourceName, model);
    return model;
  }
}
