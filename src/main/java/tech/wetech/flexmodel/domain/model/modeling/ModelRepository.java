package tech.wetech.flexmodel.domain.model.modeling;

import tech.wetech.flexmodel.Index;
import tech.wetech.flexmodel.Model;
import tech.wetech.flexmodel.TypedField;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author cjbi
 */
public interface ModelRepository {

  List<Model> findAll(String datasourceName);

  List<Model> findModels(String datasourceName);

  Optional<Model> findModel(String datasourceName, String modelName);

  Model createModel(String datasourceName, Model model);

  void dropModel(String datasourceName, String modelName);

  TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field);

  TypedField<?, ?> modifyField(String datasourceName, TypedField<?, ?> field);

  void dropField(String datasourceName, String modelName, String fieldName);

  Index createIndex(String datasourceName, Index index);

  void dropIndex(String datasourceName, String modelName, String indexName);

  List<Model>  syncModels(String datasourceName, Set<String> modelName);

  void importModels(String datasourceName, String script);
}
