package tech.wetech.flexmodel.domain.model.api;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiLogRepository {

  List<ApiLog> find(String filter, Integer current, Integer pageSize);

  ApiLog save(ApiLog record);

  void delete(String condition);
}
