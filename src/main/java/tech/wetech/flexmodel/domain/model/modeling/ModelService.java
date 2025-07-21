package tech.wetech.flexmodel.domain.model.modeling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Index;
import tech.wetech.flexmodel.SchemaObject;
import tech.wetech.flexmodel.TypedField;
import tech.wetech.flexmodel.parser.impl.ParseException;

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

  public List<SchemaObject> findAll(String datasourceName) {
    return modelRepository.findAll(datasourceName);
  }

  public List<SchemaObject> findModels(String datasourceName) {
    return modelRepository.findModels(datasourceName);
  }

  public Optional<SchemaObject> findModel(String datasourceName, String modelName) {
    return modelRepository.findModel(datasourceName, modelName);
  }

  public SchemaObject createModel(String datasourceName, SchemaObject model) {
    return modelRepository.createModel(datasourceName, model);
  }

  public void dropModel(String datasourceName, String modelName) {
    modelRepository.dropModel(datasourceName, modelName);
  }

  public TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field) {
    return modelRepository.createField(datasourceName, field);
  }

  public TypedField<?, ?> modifyField(String datasourceName, TypedField<?, ?> field) {
    return modelRepository.modifyField(datasourceName, field);
  }

  public void dropField(String datasourceName, String modelName, String fieldName) {
    modelRepository.dropField(datasourceName, modelName, fieldName);
  }

  public Index createIndex(String datasourceName, Index index) {
    return modelRepository.createIndex(datasourceName, index);
  }

  public Index modifyIndex(String datasourceName, Index index) {
    modelRepository.dropIndex(datasourceName, index.getModelName(), index.getName());
    modelRepository.createIndex(datasourceName, index);
    return index;
  }

  public void dropIndex(String datasourceName, String modelName, String indexName) {
    modelRepository.dropIndex(datasourceName, modelName, indexName);
  }

  public List<SchemaObject> syncModels(String datasourceName, Set<String> models) {
    return modelRepository.syncModels(datasourceName, models);
  }

  public void importModels(String datasourceName, String script, String type) {
    modelRepository.importModels(datasourceName, script, type);
  }

  public List<SchemaObject> executeIdl(String datasourceName, String idl) throws ParseException {
    return modelRepository.executeIdl(datasourceName, idl);
  }
}
