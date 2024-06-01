package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.data.DataService;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DataApplicationService {

  @Inject
  DataService dataService;

  public List<Map<String, Object>> findList(String datasourceName, String modelName) {
    return dataService.findList(datasourceName, modelName);
  }

}
