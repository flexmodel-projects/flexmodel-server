package tech.wetech.flexmodel.domain.model.modeling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Index;
import tech.wetech.flexmodel.Model;
import tech.wetech.flexmodel.TypedField;

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

  public List<Model> findAll(String datasourceName) {
    return modelRepository.findAll(datasourceName);
  }

  public List<Model> findModels(String datasourceName) {
    return modelRepository.findModels(datasourceName);
  }

  public Optional<Model> findModel(String datasourceName, String modelName) {
    return modelRepository.findModel(datasourceName, modelName);
  }

  public Model createModel(String datasourceName, Model model) {
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

  public List<Model> syncModels(String datasourceName, Set<String> models) {
    return modelRepository.syncModels(datasourceName, models);
  }

  public void importModels(String datasourceName, String script) {
    modelRepository.importModels(datasourceName, script);
  }
}
