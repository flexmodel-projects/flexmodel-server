package tech.wetech.flexmodel.domain.model.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.ApiRequestLog;
import tech.wetech.flexmodel.dsl.Predicate;

import java.time.LocalDateTime;
import java.util.List;

import static tech.wetech.flexmodel.codegen.System.apiRequestLog;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class ApiLogRequestService {

  @Inject
  ApiRequestLogRepository apiLogRepository;

  public ApiRequestLog create(ApiRequestLog apiRequestLog) {
    return apiLogRepository.save(apiRequestLog);
  }

  public List<ApiRequestLog> find(Predicate filter, Integer current, Integer pageSize) {
    return apiLogRepository.find(filter, current, pageSize);
  }

  public long count(Predicate filter) {
    return apiLogRepository.count(filter);
  }

  public List<LogStat> stat(Predicate filter, String fmt) {
    return apiLogRepository.stat(filter, fmt);
  }

  public List<LogApiRank> ranking(Predicate filter) {
    return apiLogRepository.ranking(filter);
  }

  public void purgeOldLogs(int maxDays) {
    log.info("Purging old logs older than {} days", maxDays);
    LocalDateTime purgeDate = LocalDateTime.now().minusDays(maxDays);
    apiLogRepository.delete(apiRequestLog.createdAt.lte(purgeDate));
  }
}
