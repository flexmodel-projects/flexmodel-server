package tech.wetech.flexmodel.domain.model.api.execution;

import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 * @author chengjinbao
 */
public class ScriptExecutionDB {

  private final String datasourceName;
  private final SessionFactory sessionFactory;

  public ScriptExecutionDB(String datasourceName, SessionFactory sessionFactory) {
    this.datasourceName = datasourceName;
    this.sessionFactory = sessionFactory;
  }

  public Map<String, Object> findById(String modelName, String id) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.data().findById(modelName, id);
    }
  }

  public List<Map<String, Object>> find(String modelName) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.data().find(modelName, p -> p);
    }
  }

  public List<Map<String, Object>> find(String modelName, Map<String, Object> map) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      Query query = new Query();
      if (map.containsKey("filter")) {
        query.setFilter(JsonUtils.getInstance().stringify(map.get("filter")));
      }
      return session.data().find(modelName, query);
    }
  }

}
