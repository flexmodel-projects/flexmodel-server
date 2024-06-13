package tech.wetech.flexmodel.domain.model.modeling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.*;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ModelService {

  @Inject
  SessionFactory sessionFactory;

  public List<Model> findModels(String datasourceName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.getAllModels();
    }
  }

  public Entity createModel(String datasourceName, Entity entity) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.createEntity(entity);
    }
  }


  public void dropModel(String datasourceName, String modelName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.dropModel(modelName);

    }
  }

  public TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.createField(field);
      return field;
    }
  }

  public void dropField(String datasourceName, String modelName, String fieldName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.dropField(modelName, fieldName);
    }
  }

  public Index createIndex(String datasourceName, Index index) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.createIndex(index);
      return index;
    }
  }

  public void dropIndex(String datasourceName, String modelName, String indexName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.dropIndex(modelName, indexName);
    }
  }

  public List<Model> refresh(String datasourceName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.syncModels();
    }
  }
}
