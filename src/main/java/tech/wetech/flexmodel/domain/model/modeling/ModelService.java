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
    Session session = sessionFactory.openSession(datasourceName);
    return session.getAllModels();
  }

  public Entity createModel(String datasourceName, Entity entity) {
    Session session = sessionFactory.openSession(datasourceName);
    return session.createEntity(entity);
  }


  public void dropModel(String datasourceName, String modelName) {
    Session session = sessionFactory.openSession(datasourceName);
    session.dropModel(modelName);
  }

  public TypedField<?, ?> createField(String datasourceName, TypedField<?, ?> field) {
    Session session = sessionFactory.openSession(datasourceName);
    session.createField(field);
    return field;
  }

  public void dropField(String datasourceName, String modelName, String fieldName) {
    Session session = sessionFactory.openSession(datasourceName);
    session.dropField(modelName, fieldName);
  }

  public Index createIndex(String datasourceName, Index index) {
    Session session = sessionFactory.openSession(datasourceName);
    session.createIndex(index);
    return index;
  }

  public void dropIndex(String datasourceName, String modelName, String indexName) {
    Session session = sessionFactory.openSession(datasourceName);
    session.dropIndex(modelName, indexName);
  }
}
