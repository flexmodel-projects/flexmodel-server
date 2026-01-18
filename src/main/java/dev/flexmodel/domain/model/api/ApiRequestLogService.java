package dev.flexmodel.domain.model.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.codegen.entity.ApiRequestLog;
import dev.flexmodel.query.Predicate;

import java.time.LocalDateTime;
import java.util.List;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
@ActivateRequestContext
public class ApiRequestLogService {

  @Inject
  ApiRequestLogRepository apiLogRepository;

  public ApiRequestLog create(ApiRequestLog apiRequestLog) {
    apiRequestLog.setCreatedAt(LocalDateTime.now());
    return apiLogRepository.save(apiRequestLog);
  }

  public List<ApiRequestLog> find(String projectId, Predicate filter, Integer current, Integer pageSize) {
    return apiLogRepository.find(projectId, filter, current, pageSize);
  }

  public long count(String projectId, Predicate filter) {
    return apiLogRepository.count(projectId, filter);
  }

  public List<LogStat> stat(String projectId, Predicate filter, String fmt) {
    return apiLogRepository.stat(projectId, filter, fmt);
  }

  public List<LogApiRank> ranking(String projectId, Predicate filter) {
    return apiLogRepository.ranking(projectId, filter);
  }

  public void purgeOldLogs(int maxDays) {
    log.info("Purging old logs older than {} days", maxDays);
    LocalDateTime purgeDate = LocalDateTime.now().minusDays(maxDays);
    apiLogRepository.delete(field(ApiRequestLog::getCreatedAt).lte(purgeDate));
  }
}
