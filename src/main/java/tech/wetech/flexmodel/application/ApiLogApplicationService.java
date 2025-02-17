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
import tech.wetech.flexmodel.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static tech.wetech.flexmodel.codegen.System.apiLog;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiLogApplicationService {

  @Inject
  ApiLogService apiLogService;

  public PageDTO<ApiLog> findApiLogs(int current, int pageSize, String keyword, LocalDateTime startDate, LocalDateTime endDate, Set<LogLevel> levels) {
    List<ApiLog> list = apiLogService.find(getCondition(keyword, startDate, endDate, levels), current, pageSize);
    long total = apiLogService.count(getCondition(keyword, startDate, endDate, levels));
    return new PageDTO<>(list, total);
  }

  public List<LogStat> stat(String keyword, LocalDateTime startDate, LocalDateTime endDate, Set<LogLevel> levels) {
    return apiLogService.stat(getCondition(keyword, startDate, endDate, levels));
  }

  private static Predicate getCondition(String keyword, LocalDateTime startDate, LocalDateTime endDate, Set<LogLevel> levels) {
    Predicate condition = Expressions.TRUE;
    if (keyword != null) {
      condition = condition.and(apiLog.data.contains(keyword));
    }
    if (startDate != null && endDate != null) {
      condition = condition.and(apiLog.createdAt.between(startDate, endDate));
    }
    if (!CollectionUtils.isEmpty(levels)) {
      condition = condition.and(apiLog.level.in(levels));
    }
    return condition;
  }

}
