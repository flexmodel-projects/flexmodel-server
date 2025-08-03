package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.modeling.ModelRepository;
import tech.wetech.flexmodel.model.*;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.parser.ASTNodeConverter;
import tech.wetech.flexmodel.parser.impl.ModelParser;
import tech.wetech.flexmodel.parser.impl.ParseException;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ModelFmRepository implements ModelRepository {

  @Inject
  SessionFactory sessionFactory;

  @Override
  @SuppressWarnings("all")
  public List<SchemaObject> findAll(String datasourceName) {
    return sessionFactory.getModels(datasourceName);
  }


  @Override
  public Optional<SchemaObject> findModel(String datasourceName, String modelName) {
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      return Optional.ofNullable(session.schema().getModel(modelName));
    }
  }

  @Override
  public SchemaObject createModel(String datasourceName, SchemaObject model) {
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      if (model instanceof EntityDefinition entity) {
        return session.schema().createEntity(entity);
      }
      if (model instanceof NativeQueryDefinition nativeQueryModelDefinition) {
        return session.schema().createNativeQueryModel(nativeQueryModelDefinition);
      }
      if (model instanceof EnumDefinition anEnumDefinition) {
        return session.schema().createEnum(anEnumDefinition);
      }
    }
    throw new RuntimeException("Unsupported model type");
  }

  @Override
  public void dropModel(String datasourceName, String modelName) {
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      session.schema().dropModel(modelName);
    }
  }

  @Override
  public TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field) {
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      session.schema().createField(field);
      return field;
    }
  }

  @Override
  public TypedField<?, ?> modifyField(String datasourceName, TypedField<?, ?> field) {
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      session.schema().modifyField(field);
      return field;
    }
  }

  @Override
  public void dropField(String datasourceName, String modelName, String fieldName) {
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      session.schema().dropField(modelName, fieldName);
    }
  }

  @Override
  public IndexDefinition createIndex(String datasourceName, IndexDefinition index) {
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      session.schema().createIndex(index);
      return index;
    }
  }

  @Override
  public void dropIndex(String datasourceName, String modelName, String indexName) {
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      session.schema().dropIndex(modelName, indexName);
    }
  }

  @Override
  public List<SchemaObject> syncModels(String datasourceName, Set<String> modelNames) {
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      return session.schema().syncModels(modelNames);
    }
  }

  @Override
  public void importModels(String datasourceName, String script, String type) {
    if (type.equals("JSON")) {
      sessionFactory.loadJSONString(datasourceName, script);
    } else if (type.equals("IDL")) {
      sessionFactory.loadIDLString(datasourceName, script);
    } else {
      throw new RuntimeException("Unsupported type");
    }
  }

  @Override
  public List<SchemaObject> executeIdl(String datasourceName, String idlString) throws ParseException {
    ModelParser parser = new ModelParser(new StringReader(idlString));
    List<ModelParser.ASTNode> ast = parser.CompilationUnit();
    List<SchemaObject> schema = new ArrayList<>();
    for (ModelParser.ASTNode obj : ast) {
      schema.add(ASTNodeConverter.toSchemaObject(obj));
    }
    try (Session session = this.sessionFactory.createSession(datasourceName)) {
      for (SchemaObject object : schema) {
        if (object instanceof EntityDefinition entity) {
          session.schema().createEntity(entity);
        }
        if (object instanceof EnumDefinition anEnumDefinition) {
          session.schema().createEnum(anEnumDefinition);
        }
      }
    }
    return schema;
  }
}
