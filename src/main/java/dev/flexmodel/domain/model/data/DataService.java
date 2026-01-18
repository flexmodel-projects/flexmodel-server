package dev.flexmodel.domain.model.data;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DataService {

  @Inject
  DataRepository dataRepository;

  public List<Map<String, Object>> findRecords(String projectId,
                                               String datasourceName,
                                               String modelName,
                                               Integer page,
                                               Integer size,
                                               String filter,
                                               String sort,
                                               boolean nestedQueryEnabled) {
    return dataRepository.findRecords(projectId, datasourceName, modelName, page, size, filter, sort, nestedQueryEnabled);
  }

  public long countRecords(String projectId, String datasourceName, String modelName, String filter) {
    return dataRepository.countRecords(projectId, datasourceName, modelName, filter);
  }

  public Map<String, Object> findOneRecord(String projectId, String datasourceName, String modelName, Object id, boolean nestedQuery) {
    return dataRepository.findOneRecord(projectId, datasourceName, modelName, id, nestedQuery);
  }

  public Map<String, Object> createRecord(String projectId, String datasourceName, String modelName, Map<String, Object> data) {
    return dataRepository.createRecord(projectId, datasourceName, modelName, data);
  }

  public Map<String, Object> updateRecord(String projectId, String datasourceName, String modelName, Object id, Map<String, Object> data) {
    return dataRepository.updateRecord(projectId, datasourceName, modelName, id, data);
  }

  public void deleteRecord(String projectId, String datasourceName, String modelName, Object id) {
    dataRepository.deleteRecord(projectId, datasourceName, modelName, id);
  }

}
