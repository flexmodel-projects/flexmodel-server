package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.application.dto.PageDTO;
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

  public PageDTO<Map<String, Object>> findPagingRecords(String datasourceName,
                                                        String modelName,
                                                        int current,
                                                        int pageSize,
                                                        String filter,
                                                        String sort) {
    List<Map<String, Object>> list = dataService.findRecords(datasourceName, modelName, current, pageSize, filter, sort);
    long total = dataService.countRecords(datasourceName, modelName, filter);
    return new PageDTO<>(list, total);
  }

  public Map<String, Object> findOneRecord(String datasourceName, String modelName, String id) {
    return dataService.findOneRecord(datasourceName, modelName, id);
  }

  public Map<String, Object> createRecord(String datasourceName, String modelName, Map<String, Object> data) {
    return dataService.createRecord(datasourceName, modelName, data);
  }

  public Map<String, Object> updateRecord(String datasourceName, String modelName, Object id, Map<String, Object> data) {
    return dataService.updateRecord(datasourceName, modelName, id, data);
  }

  public void deleteRecord(String datasourceName, String modelName, Object id) {
    dataService.deleteRecord(datasourceName, modelName, id);
  }

}
