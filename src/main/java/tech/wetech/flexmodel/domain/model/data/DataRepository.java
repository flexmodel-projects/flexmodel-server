package tech.wetech.flexmodel.domain.model.data;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public interface DataRepository {

  List<Map<String, Object>> findRecords(String datasourceName,
                                        String modelName,
                                        Integer current,
                                        Integer pageSize,
                                        String filter,
                                        String sort,
                                        boolean deep);

  long countRecords(String datasourceName, String modelName, String filter);

  Map<String, Object> findOneRecord(String datasourceName, String modelName, Object id, boolean deep);

  Map<String, Object> createRecord(String datasourceName, String modelName, Map<String, Object> data);

  Map<String, Object> updateRecord(String datasourceName, String modelName, Object id, Map<String, Object> data);

  void deleteRecord(String datasourceName, String modelName, Object id);
}
