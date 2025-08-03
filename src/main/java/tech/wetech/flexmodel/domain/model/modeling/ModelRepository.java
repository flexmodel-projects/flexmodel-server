package tech.wetech.flexmodel.domain.model.modeling;

import tech.wetech.flexmodel.model.IndexDefinition;
import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.parser.impl.ParseException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author cjbi
 */
public interface ModelRepository {

  List<SchemaObject> findAll(String datasourceName);

  Optional<SchemaObject> findModel(String datasourceName, String modelName);

  SchemaObject createModel(String datasourceName, SchemaObject model);

  void dropModel(String datasourceName, String modelName);

  TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field);

  TypedField<?, ?> modifyField(String datasourceName, TypedField<?, ?> field);

  void dropField(String datasourceName, String modelName, String fieldName);

  IndexDefinition createIndex(String datasourceName, IndexDefinition index);

  void dropIndex(String datasourceName, String modelName, String indexName);

  List<SchemaObject> syncModels(String datasourceName, Set<String> modelName);

  void importModels(String datasourceName, String script, String type);

  List<SchemaObject> executeIdl(String datasourceName, String idl) throws ParseException;
}
