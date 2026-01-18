package dev.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.application.dto.PageDTO;
import dev.flexmodel.domain.model.data.DataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DataApplicationService {

  @Inject
  DataService dataService;

  public PageDTO<Map<String, Object>> findPagingRecords(String projectId,
                                                        String datasourceName,
                                                        String modelName,
                                                        int page,
                                                        int size,
                                                        String filter,
                                                        String sort,
                                                        boolean nestedQuery) {
    List<Map<String, Object>> list = dataService.findRecords(projectId, datasourceName, modelName, page, size, filter, sort, nestedQuery);
    long total = dataService.countRecords(projectId, datasourceName, modelName, filter);
    return new PageDTO<>(list, total);
  }

  public Map<String, Object> findOneRecord(String projectId, String datasourceName, String modelName, String id, boolean nestedQuery) {
    return dataService.findOneRecord(projectId, datasourceName, modelName, id, nestedQuery);
  }

  public Map<String, Object> createRecord(String projectId, String datasourceName, String modelName, Map<String, Object> data) {
    return dataService.createRecord(projectId, datasourceName, modelName, data);
  }

  public Map<String, Object> updateRecord(String projectId, String datasourceName, String modelName, Object id, Map<String, Object> data) {
    return dataService.updateRecord(projectId, datasourceName, modelName, id, data);
  }

  public void deleteRecord(String projectId, String datasourceName, String modelName, Object id) {
    dataService.deleteRecord(projectId, datasourceName, modelName, id);
  }

  public Map<String, Object> updateRecordIgnoreNull(String projectId, String datasourceName, String modelName, String id, Map<String, Object> record) {
    Map<String, Object> oldData = dataService.findOneRecord(projectId, datasourceName, modelName, id, false);
    Map<String, Object> mergeData = new HashMap<>(oldData);
    mergeData.putAll(record);
    return dataService.updateRecord(projectId, datasourceName, modelName, id, mergeData);
  }
}
