package dev.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.domain.model.connect.DatasourceService;
import dev.flexmodel.domain.model.connect.NativeQueryResult;
import dev.flexmodel.domain.model.connect.ValidateResult;
import dev.flexmodel.domain.model.modeling.ModelService;
import dev.flexmodel.model.*;
import dev.flexmodel.model.field.TypedField;
import dev.flexmodel.parser.impl.ParseException;

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
    return datasourceService.findAll(projectId);
  }

  public Datasource createDatasource(String projectId, Datasource datasource) {
    return datasourceService.createDatasource(projectId, datasource);
  }

  public Datasource updateDatasource(String projectId, Datasource record) {
    return datasourceService.updateDatasource(projectId, record);
  }

  public void deleteDatasource(String projectId, String datasourceName) {
    datasourceService.deleteDatasource(projectId, datasourceName);
  }

  public List<SchemaObject> findModels(String projectId, String datasourceName) {
    return modelService.findAll(projectId, datasourceName);
  }

  public SchemaObject createModel(String projectId, String datasourceName, SchemaObject model) {
    return modelService.createModel(projectId, datasourceName, model);
  }

  public void dropModel(String projectId, String datasourceName, String modelName) {
    modelService.dropModel(projectId, datasourceName, modelName);
  }

  public TypedField<?, ?> createField(String projectId, String datasourceName, TypedField<?, ?> field) {
    return modelService.createField(projectId, datasourceName, field);
  }

  public TypedField<?, ?> modifyField(String projectId, String datasourceName, TypedField<?, ?> field) {
    return modelService.modifyField(projectId, datasourceName, field);
  }

  public void dropField(String projectId, String datasourceName, String modelName, String fieldName) {
    modelService.dropField(projectId, datasourceName, modelName, fieldName);
  }

  public IndexDefinition createIndex(String projectId, String datasourceName, IndexDefinition index) {
    return modelService.createIndex(projectId, datasourceName, index);
  }

  public IndexDefinition modifyIndex(String projectId, String datasourceName, IndexDefinition index) {
    return modelService.modifyIndex(projectId, datasourceName, index);
  }

  public void dropIndex(String projectId, String datasourceName, String modelName, String indexName) {
    modelService.dropIndex(projectId, datasourceName, modelName, indexName);
  }

  public ValidateResult validateConnection(String projectId, Datasource datasource) {
    return datasourceService.validate(datasource);
  }

  public List<String> getPhysicsModelNames(Datasource datasource) {
    return datasourceService.getPhysicsModelNames(datasource);
  }

  public List<SchemaObject> syncModels(String projectId, String datasourceName, Set<String> models) {
    return modelService.syncModels(projectId, datasourceName, models);
  }

  public void importModels(String projectId, String datasourceName, String script, String type) {
    modelService.importModels(projectId, datasourceName, script, type);
  }

  public NativeQueryResult executeNativeQuery(String projectId, String datasourceName, String statement, Map<String, Object> parameters) {
    return datasourceService.executeNativeQuery(projectId, datasourceName, statement, parameters);
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
    modelService.dropModel(projectId, datasourceName, modelName);
    modelService.createModel(projectId, datasourceName, model);
    return model;
  }

  public SchemaObject findModel(String projectId, String datasourceName, String modelName) {
    return modelService.findModel(projectId, datasourceName, modelName).orElseThrow(() -> new RuntimeException("Model not found"));
  }

  public List<SchemaObject> executeIdl(String projectId, String datasourceName, String idl) throws ParseException {
    return modelService.executeIdl(projectId, datasourceName, idl);
  }
}
