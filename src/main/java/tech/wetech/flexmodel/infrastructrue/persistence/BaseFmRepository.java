package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.inject.Inject;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.Session;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author cjbi
 */
public abstract class BaseFmRepository<T, ID> {

  @Inject
  protected Session session;

  private String entityName;

  private Class<T> entityType;

  public List<T> findAll() {
    return session.find(getEntityName(), query -> query, getEntityType());
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

  @SuppressWarnings("all")
  public T save(T record) {
    Entity entity = (Entity) session.getModel(getEntityName());
    Map<String, Object> recordMap = JsonUtils.getInstance().convertValue(record, Map.class);
    if (isNew(record)) {
      session.insert(getEntityName(), recordMap, id -> recordMap.put(entity.getIdField().getName(), id));
    } else {
      session.updateById(getEntityName(), recordMap, recordMap.get(entity.getIdField().getName()));
    }
    return JsonUtils.getInstance().convertValue(recordMap, getEntityType());
  }

  public void delete(ID id) {
    session.deleteById(getEntityName(), id);
  }

  @SuppressWarnings("all")
  private boolean isNew(T record) {
    Entity entity = (Entity) session.getModel(getEntityName());
    Map<String, Object> recordMap = JsonUtils.getInstance().convertValue(record, Map.class);
    Object id = recordMap.get(entity.getIdField());
    return id == null || !session.existsById(getEntityName(), id);
  }

  public Optional<T> findById(ID id) {
    return Optional.ofNullable(session.findById(getEntityName(), id, getEntityType()));
  }
}
