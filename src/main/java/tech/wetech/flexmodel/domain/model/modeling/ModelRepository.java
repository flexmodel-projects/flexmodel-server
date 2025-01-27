package tech.wetech.flexmodel.domain.model.modeling;

import tech.wetech.flexmodel.Index;
import tech.wetech.flexmodel.TypeWrapper;
import tech.wetech.flexmodel.TypedField;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author cjbi
 */
public interface ModelRepository {

  List<TypeWrapper> findAll(String datasourceName);

  List<TypeWrapper> findModels(String datasourceName);

  Optional<TypeWrapper> findModel(String datasourceName, String modelName);

  TypeWrapper createModel(String datasourceName, TypeWrapper model);

  void dropModel(String datasourceName, String modelName);

  TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field);

  TypedField<?, ?> modifyField(String datasourceName, TypedField<?, ?> field);

  void dropField(String datasourceName, String modelName, String fieldName);

  Index createIndex(String datasourceName, Index index);

  void dropIndex(String datasourceName, String modelName, String indexName);

  List<TypeWrapper>  syncModels(String datasourceName, Set<String> modelName);

  void importModels(String datasourceName, String script);
}
