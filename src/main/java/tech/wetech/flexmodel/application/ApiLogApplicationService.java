package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.criterion.Example;
import tech.wetech.flexmodel.domain.model.api.ApiLogService;
import tech.wetech.flexmodel.domain.model.api.LogStat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiLogApplicationService {

  @Inject
  ApiLogService apiLogService;

  public List<ApiLog> findApiLogs(int current, int pageSize, String keyword, LocalDateTime startDate, LocalDateTime endDate, Set<String> level) {

    return apiLogService.find(f -> getCriteria(keyword, startDate, endDate, level, f), current, pageSize);
  }

  public List<LogStat> stat(String keyword, LocalDateTime startDate, LocalDateTime endDate, Set<String> level) {
    return apiLogService.stat(f -> getCriteria(keyword, startDate, endDate, level, f));
  }

  private static Example.Criteria getCriteria(String keyword, LocalDateTime startDate, LocalDateTime endDate, Set<String> level, Example.Criteria f) {
    if (keyword != null) {
      f.contains("data", keyword);
    }
    if (startDate != null && endDate != null) {
      f.between("createdAt", startDate, endDate);
    }
    if (level != null) {
      f.in("level", level);
    }
    return f;
  }

}
