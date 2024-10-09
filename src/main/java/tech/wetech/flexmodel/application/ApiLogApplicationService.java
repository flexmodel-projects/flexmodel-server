package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.domain.model.api.ApiLogService;
import tech.wetech.flexmodel.domain.model.api.LogStat;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiLogApplicationService {

  @Inject
  ApiLogService apiLogService;

  public List<ApiLog> findApiLogs(String filter, int current, int pageSize) {
    return apiLogService.find(filter, current, pageSize);
  }
  public List<LogStat> stat(String filter) {
    return apiLogService.stat(filter);
  }

}
