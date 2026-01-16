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

  List<SchemaObject> findAll(String projectId, String datasourceName);

  Optional<SchemaObject> findModel(String projectId, String datasourceName, String modelName);

  SchemaObject createModel(String projectId, String datasourceName, SchemaObject model);

  void dropModel(String projectId, String datasourceName, String modelName);

  TypedField<?, ?> createField(String projectId, String datasourceName, TypedField<?, ?> field);

  TypedField<?, ?> modifyField(String projectId, String datasourceName, TypedField<?, ?> field);

  void dropField(String projectId, String datasourceName, String modelName, String fieldName);

  IndexDefinition createIndex(String projectId, String datasourceName, IndexDefinition index);

  void dropIndex(String projectId, String datasourceName, String modelName, String indexName);

  List<SchemaObject> syncModels(String projectId, String datasourceName, Set<String> modelName);

  void importModels(String projectId, String datasourceName, String script, String type);

  List<SchemaObject> executeIdl(String projectId, String datasourceName, String idl) throws ParseException;

  Integer count(String projectId);

}
