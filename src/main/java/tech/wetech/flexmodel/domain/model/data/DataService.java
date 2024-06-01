package tech.wetech.flexmodel.domain.model.data;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Session;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DataService {

  @Inject
  Session session;

  public List<Map<String, Object>> findList(String datasourceName, String modelName) {
    return session.find(modelName, query -> query);
  }

}
