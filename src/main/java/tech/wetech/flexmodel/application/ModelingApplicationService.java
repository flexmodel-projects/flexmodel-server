package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.domain.model.connect.NativeQueryResult;
import tech.wetech.flexmodel.domain.model.connect.ValidateResult;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;
import tech.wetech.flexmodel.model.*;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.parser.impl.ParseException;

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

  public List<Datasource> findDatasourceList(String projectId) {
    return datasourceService.findAll();
  }

  public Datasource createDatasource(Datasource datasource) {
    return datasourceService.createDatasource(datasource);
  }

  public Datasource updateDatasource(String projectId, Datasource record) {
    return datasourceService.updateDatasource(record);
  }

  public void deleteDatasource(String datasourceName, String name) {
    datasourceService.deleteDatasource(datasourceName);
  }

  public List<SchemaObject> findModels(String projectId, String datasourceName) {
    return modelService.findAll(datasourceName);
  }

  public SchemaObject createModel(String projectId, String datasourceName, SchemaObject model) {
    return modelService.createModel(datasourceName, model);
  }

  public void dropModel(String projectId, String datasourceName, String modelName) {
    modelService.dropModel(datasourceName, modelName);
  }

  public TypedField<?, ?> createField(String projectId, String datasourceName, TypedField<?, ?> field) {
    return modelService.createField(datasourceName, field);
  }

  public TypedField<?, ?> modifyField(String projectId, String datasourceName, TypedField<?, ?> field) {
    return modelService.modifyField(datasourceName, field);
  }

  public void dropField(String projectId, String datasourceName, String modelName, String fieldName) {
    modelService.dropField(datasourceName, modelName, fieldName);
  }

  public IndexDefinition createIndex(String projectId, String datasourceName, IndexDefinition index) {
    return modelService.createIndex(datasourceName, index);
  }

  public IndexDefinition modifyIndex(String projectId, String datasourceName, IndexDefinition index) {
    return modelService.modifyIndex(datasourceName, index);
  }

  public void dropIndex(String projectId, String datasourceName, String modelName, String indexName) {
    modelService.dropIndex(datasourceName, modelName, indexName);
  }

  public ValidateResult validateConnection(String projectId, Datasource datasource) {
    return datasourceService.validate(datasource);
  }

  public List<String> getPhysicsModelNames(String projectId, Datasource datasource) {
    return datasourceService.getPhysicsModelNames(datasource);
  }

  public List<SchemaObject> syncModels(String projectId, String datasourceName, Set<String> models) {
    return modelService.syncModels(datasourceName, models);
  }

  public void importModels(String projectId, String datasourceName, String script, String type) {
    modelService.importModels(datasourceName, script, type);
  }

  public NativeQueryResult executeNativeQuery(String projectId, String datasourceName, String statement, Map<String, Object> parameters) {
    return datasourceService.executeNativeQuery(datasourceName, statement, parameters);
  }

  public SchemaObject modifyModel(String projectId, String datasourceName, String modelName, SchemaObject model) {
    if (model instanceof EntityDefinition) {
      throw new RuntimeException("Unsupported model type");
    }
    if (model instanceof NativeQueryDefinition nativeQueryModel) {
      nativeQueryModel.setName(modelName);
    }
    if (model instanceof EnumDefinition anEnum) {
      anEnum.setName(modelName);
    }
    modelService.dropModel(datasourceName, modelName);
    modelService.createModel(datasourceName, model);
    return model;
  }

  public SchemaObject findModel(String projectId, String datasourceName, String modelName) {
    return modelService.findModel(datasourceName, modelName).orElseThrow(() -> new RuntimeException("Model not found"));
  }

  public List<SchemaObject> executeIdl(String projectId, String datasourceName, String idl) throws ParseException {
    return modelService.executeIdl(datasourceName, idl);
  }
}
