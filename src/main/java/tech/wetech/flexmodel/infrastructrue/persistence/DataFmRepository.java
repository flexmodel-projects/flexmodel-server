package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.domain.model.data.DataRepository;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DataFmRepository implements DataRepository {

  @Inject
  SessionFactory sessionFactory;

  @Override
  public List<Map<String, Object>> findRecords(String datasourceName,
                                               String modelName,
                                               Integer current,
                                               Integer pageSize,
                                               String filter,
                                               String sort) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.find(modelName, query -> {
        query.setFilter(filter);
        if (pageSize != null) {
          query.setLimit(pageSize);
          if (current != null) {
            query.setOffset((current - 1) * pageSize);
          }
        }
        return query;
      });
    }
  }

  @Override
  public long countRecords(String datasourceName, String modelName, String filter) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.count(modelName, query -> query.setFilter(filter));
    }
  }

  @Override
  public Map<String, Object> findOneRecord(String datasourceName, String modelName, Object id) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.findById(modelName, id);
    }
  }

  @Override
  public Map<String, Object> createRecord(String datasourceName, String modelName, Map<String, Object> data) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      Entity entity = (Entity) session.getModel(modelName);
      session.insert(modelName, data, id -> data.put(entity.findIdField().orElseThrow().getName(), id));
      return data;
    }
  }

  @Override
  public Map<String, Object> updateRecord(String datasourceName, String modelName, Object id, Map<String, Object> data) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.updateById(modelName, data, id);
      return data;
    }
  }

  @Override
  public void deleteRecord(String datasourceName, String modelName, Object id) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.deleteById(modelName, id);
    }
  }

}
