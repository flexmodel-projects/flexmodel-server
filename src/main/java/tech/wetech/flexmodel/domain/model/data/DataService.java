package tech.wetech.flexmodel.domain.model.data;

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

  public List<Map<String, Object>> findRecords(String datasourceName,
                                               String modelName,
                                               Integer page,
                                               Integer size,
                                               String filter,
                                               String sort,
                                               boolean nestedQueryEnabled) {
    return dataRepository.findRecords(datasourceName, modelName, page, size, filter, sort, nestedQueryEnabled);
  }

  public long countRecords(String datasourceName, String modelName, String filter) {
    return dataRepository.countRecords(datasourceName, modelName, filter);
  }

  public Map<String, Object> findOneRecord(String datasourceName, String modelName, Object id, boolean nestedQuery) {
    return dataRepository.findOneRecord(datasourceName, modelName, id, nestedQuery);
  }

  public Map<String, Object> createRecord(String datasourceName, String modelName, Map<String, Object> data) {
    return dataRepository.createRecord(datasourceName, modelName, data);
  }

  public Map<String, Object> updateRecord(String datasourceName, String modelName, Object id, Map<String, Object> data) {
    return dataRepository.updateRecord(datasourceName, modelName, id, data);
  }

  public void deleteRecord(String datasourceName, String modelName, Object id) {
    dataRepository.deleteRecord(datasourceName, modelName, id);
  }

}
