package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.inject.Inject;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.infrastructrue.FmEngineSessions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author cjbi
 */
public abstract class BaseFmRepository<T, ID> {

  @Inject
  protected SessionFactory sessionFactory;

  private String entityName;

  private Class<T> entityType;

  protected <R> R withSession(Function<Session, R> fn) {
    return withSession(FmEngineSessions.SYSTEM_DS_KEY, fn);
  }

  protected <R> R withSession(String identifier, Function<Session, R> fn) {
    try (Session session = sessionFactory.createSession(identifier)) {
      return fn.apply(session);
    }
  }


  public List<T> findAll() {
    return withSession(session -> session.find(getEntityName(), query -> query, getEntityType()));
  }

  public String getEntityName() {
    if (entityName == null) {
      entityName = getEntityType().getSimpleName();
    }
    return entityName;
  }

  public Class<T> getEntityType() {
    if (entityType == null) {
      entityType = lookupEntityClass(this.getClass());
    }
    return entityType;
  }

  @SuppressWarnings("unchecked")
  public Class<T> lookupEntityClass(Class<?> clazz) {
    if (clazz == null) {
      throw new RuntimeException(this.getClass().getSimpleName() + " entity not found");
    }
    Type genericSuperclass = clazz.getGenericSuperclass();
    if (genericSuperclass instanceof ParameterizedType type && type.getActualTypeArguments().length > 0) {
      return (Class<T>) type.getActualTypeArguments()[0];
    }
    return lookupEntityClass(clazz.getSuperclass());
  }

  public List<T> find(String filter, String sort, Integer current, Integer pageSize) {
    String entityName = getEntityName();
    Class<T> resultType = getEntityType();
    return withSession(session -> session.find(entityName, query -> {
      if (filter != null) {
        query.setFilter(filter);
      }
      if (sort != null) {
        // todo
//        query.setSort()
      }
      if (pageSize != null) {
        query.setLimit(pageSize);
        if (current != null) {
          query.setOffset((current - 1) * pageSize);
        }
      }
      return query;
    }, resultType));
  }

  @SuppressWarnings("all")
  public T save(T record) {
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

  public void delete(ID id) {
    withSession(session -> session.deleteById(getEntityName(), id));
  }

  @SuppressWarnings("all")
  private boolean isNew(T record) {
    return withSession(session -> {
      Entity entity = (Entity) session.getModel(getEntityName());
      Map<String, Object> recordMap = JsonUtils.getInstance().convertValue(record, Map.class);
      Object id = recordMap.get(entity.findIdField().orElseThrow().getName());
      return id == null || !session.existsById(getEntityName(), id);
    });
  }

  public Optional<T> findById(ID id) {
    return withSession(session -> Optional.ofNullable(session.findById(getEntityName(), id, getEntityType())));
  }
}
