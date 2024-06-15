package tech.wetech.flexmodel.domain.model.modeling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Index;
import tech.wetech.flexmodel.TypedField;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ModelService {

  @Inject
  ModelRepository modelFmRepository;

  public List<Entity> findAll(String datasourceName) {
    return modelFmRepository.findAll(datasourceName);
  }

  public List<Entity> findModels(String datasourceName) {
    return modelFmRepository.findModels(datasourceName);
  }

  public Entity createModel(String datasourceName, Entity entity) {
    return modelFmRepository.createModel(datasourceName, entity);
  }

  public void dropModel(String datasourceName, String modelName) {
    modelFmRepository.dropModel(datasourceName, modelName);
  }

  public TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field) {
    return modelFmRepository.createField(datasourceName, field);
  }

  public void dropField(String datasourceName, String modelName, String fieldName) {
    modelFmRepository.dropField(datasourceName, modelName, fieldName);
  }

  public Index createIndex(String datasourceName, Index index) {
    return modelFmRepository.createIndex(datasourceName, index);
  }

  public void dropIndex(String datasourceName, String modelName, String indexName) {
    modelFmRepository.dropIndex(datasourceName, modelName, indexName);
  }

  public List<Entity> refresh(String datasourceName) {
    return modelFmRepository.refresh(datasourceName);
  }

}
