package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.domain.model.modeling.ModelRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ModelFmRepository extends BaseFmRepository<Model, String> implements ModelRepository {

  @Override
  @SuppressWarnings("all")
  public List<Entity> findAll(String datasourceName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return (List) session.getAllModels();
    }
  }

  @Override
  @SuppressWarnings("all")
  public List<Entity> findModels(String datasourceName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return (List) session.getAllModels();
    }
  }

  @Override
  public Optional<Entity> findModel(String datasourceName, String modelName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return Optional.ofNullable((Entity) session.getModel(modelName));
    }
  }

  @Override
  public Entity createModel(String datasourceName, Entity entity) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.createEntity(entity);
    }
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
  @SuppressWarnings("all")
  public List<Entity> refresh(String datasourceName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return (List) session.syncModels();
    }
  }
}
