package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.codegen.enumeration.LogLevel;
import tech.wetech.flexmodel.domain.model.api.ApiLogService;
import tech.wetech.flexmodel.domain.model.api.LogStat;
import tech.wetech.flexmodel.dsl.Expressions;
import tech.wetech.flexmodel.dsl.Predicate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.codegen.System.apiLog;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiLogApplicationService {

  @Inject
  ApiLogService apiLogService;

  public PageDTO<ApiLog> findApiLogs(int current, int pageSize, String keyword, LocalDateTime startDate, LocalDateTime endDate, Set<String> level) {
    List<ApiLog> list = apiLogService.find(getCondition(keyword, startDate, endDate, level), current, pageSize);
    long total = apiLogService.count(getCondition(keyword, startDate, endDate, level));
    return new PageDTO<>(list, total);
  }

  public List<LogStat> stat(String keyword, LocalDateTime startDate, LocalDateTime endDate, Set<String> level) {
    return apiLogService.stat( getCondition(keyword, startDate, endDate, level));
  }

  private static Predicate getCondition(String keyword, LocalDateTime startDate, LocalDateTime endDate, Set<String> level) {
    Predicate condition = Expressions.TRUE;
    if (keyword != null) {
      condition = condition.and(apiLog.data.contains(keyword));
    }
    if (startDate != null && endDate != null) {
      condition = condition.and(apiLog.createdAt.between(startDate, endDate));
    }
    if (level != null) {
      Set<LogLevel> logLevels = level.stream().map(LogLevel::valueOf).collect(Collectors.toSet());
      condition = condition.and(apiLog.level.in(logLevels));
    }
    return condition;
  }

}
