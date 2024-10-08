package tech.wetech.flexmodel.domain.model.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiLog;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiLogService {

  @Inject
  ApiLogRepository apiLogRepository;

  public ApiLog create(ApiLog apiLog) {
    return apiLogRepository.save(apiLog);
  }

  public List<ApiLog> find(String filter, Integer current, Integer pageSize) {
    String qFilter = null;
    if (filter != null && !filter.isEmpty()) {
      qFilter = String.format("""
        { "contains": [{ "var": "data" }, "%s"] }
        """, filter);
    }
    return apiLogRepository.find(qFilter, current, pageSize);
  }

  public List<LogStat> stat(String filter) {
    String qFilter = null;
    if (filter != null && !filter.isEmpty()) {
      qFilter = String.format("""
        { "contains": [{ "var": "data" }, "%s"] }
        """, filter);
    }
    return apiLogRepository.stat(qFilter);
  }

}
