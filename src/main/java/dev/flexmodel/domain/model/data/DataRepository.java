package dev.flexmodel.domain.model.data;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public interface DataRepository {

  List<Map<String, Object>> findRecords(String projectId,
                                        String datasourceName,
                                        String modelName,
                                        Integer page,
                                        Integer size,
                                        String filter,
                                        String sort,
                                        boolean nestedQueryEnabled);

  long countRecords(String projectId, String datasourceName, String modelName, String filter);

  Map<String, Object> findOneRecord(String projectId, String datasourceName, String modelName, Object id, boolean nestedQueryEnabled);

  Map<String, Object> createRecord(String projectId, String datasourceName, String modelName, Map<String, Object> data);

  Map<String, Object> updateRecord(String projectId, String datasourceName, String modelName, Object id, Map<String, Object> data);

  void deleteRecord(String projectId, String datasourceName, String modelName, Object id);
}
