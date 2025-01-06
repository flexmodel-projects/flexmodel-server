package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.codegen.StringUtils;
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
                                               String sort,
                                               boolean deep) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.find(modelName, query -> {
        if (!StringUtils.isBlank(filter)) {
          query.setFilter(filter);
        }
        if (pageSize != null) {
          query.withPage(page -> {
            page.setPageSize(pageSize);
            if (current != null) {
              page.setPageNumber(current);
            }
            return page;
          });
        }
        query.setDeep(deep);
        return query;
      });
    }
  }

  @Override
  public long countRecords(String datasourceName, String modelName, String filter) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.count(modelName, query -> {
        if (!StringUtils.isBlank(filter)) {
          query.setFilter(filter);
        }
        return query;
      });
    }
  }

  @Override
  public Map<String, Object> findOneRecord(String datasourceName, String modelName, Object id, boolean deep) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.findById(modelName, id, deep);
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
