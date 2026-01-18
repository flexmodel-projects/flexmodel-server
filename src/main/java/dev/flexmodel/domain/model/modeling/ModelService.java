package dev.flexmodel.domain.model.modeling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.model.IndexDefinition;
import dev.flexmodel.model.SchemaObject;
import dev.flexmodel.model.field.TypedField;
import dev.flexmodel.parser.impl.ParseException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ModelService {

  @Inject
  ModelRepository modelRepository;

  public Integer count(String projectId) {
    return modelRepository.count(projectId);
  }

  public List<SchemaObject> findAll(String projectId, String datasourceName) {
    return modelRepository.findAll(projectId, datasourceName);
  }

  public Optional<SchemaObject> findModel(String projectId, String datasourceName, String modelName) {
    return modelRepository.findModel(projectId, datasourceName, modelName);
  }

  public SchemaObject createModel(String projectId, String datasourceName, SchemaObject model) {
    return modelRepository.createModel(projectId, datasourceName, model);
  }

  public void dropModel(String projectId, String datasourceName, String modelName) {
    modelRepository.dropModel(projectId, datasourceName, modelName);
  }

  public TypedField<?, ?> createField(String projectId, String datasourceName, TypedField<?, ?> field) {
    return modelRepository.createField(projectId, datasourceName, field);
  }

  public TypedField<?, ?> modifyField(String projectId, String datasourceName, TypedField<?, ?> field) {
    return modelRepository.modifyField(projectId, datasourceName, field);
  }

  public void dropField(String projectId, String datasourceName, String modelName, String fieldName) {
    modelRepository.dropField(projectId, datasourceName, modelName, fieldName);
  }

  public IndexDefinition createIndex(String projectId, String datasourceName, IndexDefinition index) {
    return modelRepository.createIndex(projectId, datasourceName, index);
  }

  public IndexDefinition modifyIndex(String projectId, String datasourceName, IndexDefinition index) {
    modelRepository.dropIndex(projectId, datasourceName, index.getModelName(), index.getName());
    modelRepository.createIndex(projectId, datasourceName, index);
    return index;
  }

  public void dropIndex(String projectId, String datasourceName, String modelName, String indexName) {
    modelRepository.dropIndex(projectId, datasourceName, modelName, indexName);
  }

  public List<SchemaObject> syncModels(String projectId, String datasourceName, Set<String> models) {
    return modelRepository.syncModels(projectId, datasourceName, models);
  }

  public void importModels(String projectId, String datasourceName, String script, String type) {
    modelRepository.importModels(projectId, datasourceName, script, type);
  }

  public List<SchemaObject> executeIdl(String projectId, String datasourceName, String idl) throws ParseException {
    return modelRepository.executeIdl(projectId, datasourceName, idl);
  }
}
