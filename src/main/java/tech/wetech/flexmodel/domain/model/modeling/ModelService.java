package tech.wetech.flexmodel.domain.model.modeling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Model;
import tech.wetech.flexmodel.Session;

import java.util.Collections;
import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ModelService {

  @Inject
  Session session;

  public List<Model> findModels(String datasourceName) {
    if ("system".equals(datasourceName)) {
      return session.getAllModels();
    }
    return Collections.emptyList();
  }


}
