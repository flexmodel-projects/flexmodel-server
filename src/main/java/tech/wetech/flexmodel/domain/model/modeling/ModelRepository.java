package tech.wetech.flexmodel.domain.model.modeling;

import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Index;
import tech.wetech.flexmodel.TypedField;

import java.util.List;
import java.util.Optional;

/**
 * @author cjbi
 */
public interface ModelRepository {

  List<Entity> findAll(String datasourceName);

  List<Entity> findModels(String datasourceName);

  Optional<Entity> findModel(String datasourceName, String modelName);

  Entity createModel(String datasourceName, Entity entity);

  void dropModel(String datasourceName, String modelName);

  TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field);

  TypedField<?, ?> modifyField(String datasourceName, TypedField<?, ?> field);

  void dropField(String datasourceName, String modelName, String fieldName);

  Index createIndex(String datasourceName, Index index);

  void dropIndex(String datasourceName, String modelName, String indexName);

  List<Entity> refresh(String datasourceName);
}
