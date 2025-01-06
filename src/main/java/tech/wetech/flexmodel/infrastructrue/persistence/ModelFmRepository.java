package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.criterion.Example;
import tech.wetech.flexmodel.domain.model.modeling.ModelRepository;
import tech.wetech.flexmodel.infrastructrue.FmEngineSessions;
import tech.wetech.flexmodel.util.JsonUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ModelFmRepository implements ModelRepository {

  @Inject
  protected SessionFactory sessionFactory;

  private String entityName;

  private Class<Model> entityType;

  protected <R> R withSession(Function<Session, R> fn) {
    return withSession(FmEngineSessions.SYSTEM_DS_KEY, fn);
  }

  protected <R> R withSession(String identifier, Function<Session, R> fn) {
    try (Session session = sessionFactory.createSession(identifier)) {
      return fn.apply(session);
    }
  }


  public List<Model> findAll() {
    return withSession(session -> session.find(getEntityName(), query -> query, getEntityType()));
  }

  public String getEntityName() {
    if (entityName == null) {
      entityName = getEntityType().getSimpleName();
    }
    return entityName;
  }

  public Class<Model> getEntityType() {
    if (entityType == null) {
      entityType = lookupEntityClass(this.getClass());
    }
    return entityType;
  }

  @SuppressWarnings("unchecked")
  public Class<Model> lookupEntityClass(Class<?> clazz) {
    if (clazz == null) {
      throw new RuntimeException(this.getClass().getSimpleName() + " entity not found");
    }
    Type genericSuperclass = clazz.getGenericSuperclass();
    if (genericSuperclass instanceof ParameterizedType type && type.getActualTypeArguments().length > 0) {
      return (Class<Model>) type.getActualTypeArguments()[0];
    }
    return lookupEntityClass(clazz.getSuperclass());
  }

  public List<Model> find(UnaryOperator<Example.Criteria> filter, Query.Sort sort, Integer current, Integer pageSize) {
    String entityName = getEntityName();
    Class<Model> resultType = getEntityType();
    return withSession(session -> session.find(entityName, query -> {
      if (filter != null) {
        query.withFilter(filter);
      }
      if (sort != null) {
        query.setSort(sort);
      }
      if (current != null && pageSize != null) {
        query.setPage(current, pageSize);
      }
      return query;
    }, resultType));
  }

  @SuppressWarnings("all")
  public Model save(Model record) {
    return withSession(session -> {
      Entity entity = (Entity) session.getModel(getEntityName());
      Map<String, Object> recordMap = JsonUtils.getInstance().convertValue(record, Map.class);
      if (isNew(record)) {
        session.insert(getEntityName(), recordMap, id -> recordMap.put(entity.findIdField().orElseThrow().getName(), id));
      } else {
        session.updateById(getEntityName(), recordMap, recordMap.get(entity.findIdField().orElseThrow().getName()));
      }
      return JsonUtils.getInstance().convertValue(recordMap, getEntityType());
    });

  }

  public void delete(String id) {
    withSession(session -> session.deleteById(getEntityName(), id));
  }

  @SuppressWarnings("all")
  private boolean isNew(Model record) {
    return withSession(session -> {
      Entity entity = (Entity) session.getModel(getEntityName());
      Map<String, Object> recordMap = JsonUtils.getInstance().convertValue(record, Map.class);
      Object id = recordMap.get(entity.findIdField().orElseThrow().getName());
      return id == null || !session.existsById(getEntityName(), id);
    });
  }

  public Optional<Model> findById(String id) {
    return withSession(session -> Optional.ofNullable(session.findById(getEntityName(), id, getEntityType())));
  }

  @Override
  @SuppressWarnings("all")
  public List<Model> findAll(String datasourceName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return (List) session.getAllModels();
    }
  }

  @Override
  @SuppressWarnings("all")
  public List<Model> findModels(String datasourceName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return (List) session.getAllModels();
    }
  }

  @Override
  public Optional<Model> findModel(String datasourceName, String modelName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return Optional.ofNullable((Entity) session.getModel(modelName));
    }
  }

  @Override
  public Model createModel(String datasourceName, Model model) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      if (model instanceof Entity entity) {
        return session.createEntity(entity);
      }
      if (model instanceof NativeQueryModel nativeQueryModel) {
        return session.createNativeQueryModel(nativeQueryModel);
      }
    }
    throw new RuntimeException("Unsupported model type");
  }

  @Override
  public void dropModel(String datasourceName, String modelName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.dropModel(modelName);
    }
  }

  @Override
  public TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.createField(field);
      return field;
    }
  }

  @Override
  public TypedField<?, ?> modifyField(String datasourceName, TypedField<?, ?> field) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.modifyField(field);
      return field;
    }
  }

  @Override
  public void dropField(String datasourceName, String modelName, String fieldName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.dropField(modelName, fieldName);
    }
  }

  @Override
  public Index createIndex(String datasourceName, Index index) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.createIndex(index);
      return index;
    }
  }

  @Override
  public void dropIndex(String datasourceName, String modelName, String indexName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.dropIndex(modelName, indexName);
    }
  }

  @Override
  public List<Model> syncModels(String datasourceName, Set<String> modelNames) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.syncModels(modelNames);
    }
  }

  @Override
  public void importModels(String datasourceName, String script) {
    sessionFactory.loadScriptString(datasourceName, script);
  }
}
