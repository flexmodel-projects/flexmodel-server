package tech.wetech.flexmodel.domain.model.data;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DataService {

  @Inject
  SessionFactory sessionFactory;

  public List<Map<String, Object>> findRecords(String datasourceName,
                                               String modelName,
                                               Integer current,
                                               Integer pageSize,
                                               String filter,
                                               String sort) {
    Session session = sessionFactory.openSession(datasourceName);
    return session.find(modelName, query -> query.setFilter(filter).setLimit(pageSize).setOffset((current - 1) * pageSize));
  }

  public long countRecords(String datasourceName, String modelName, String filter) {
    Session session = sessionFactory.openSession(datasourceName);
    return session.count(modelName, query -> query.setFilter(filter));
  }

  public Map<String, Object> findOneRecord(String datasourceName, String modelName, Object id) {
    Session session = sessionFactory.openSession(datasourceName);
    return session.findById(modelName, id);
  }

  public Map<String, Object> createRecord(String datasourceName, String modelName, Map<String, Object> data) {
    Session session = sessionFactory.openSession(datasourceName);
    Entity entity = (Entity) session.getModel(modelName);
    session.insert(modelName, data, id -> data.put(entity.getIdField().getName(), id));
    return data;
  }

  public Map<String, Object> updateRecord(String datasourceName, String modelName, Object id, Map<String, Object> data) {
    Session session = sessionFactory.openSession(datasourceName);
    session.updateById(modelName, data, id);
    return data;
  }

  public void deleteRecord(String datasourceName, String modelName, Object id) {
    Session session = sessionFactory.openSession(datasourceName);
    session.deleteById(modelName, id);
  }

}
