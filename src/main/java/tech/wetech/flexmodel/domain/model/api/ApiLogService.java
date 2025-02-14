package tech.wetech.flexmodel.domain.model.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.dsl.Predicate;

import java.time.LocalDateTime;
import java.util.List;

import static tech.wetech.flexmodel.codegen.System.apiLog;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class ApiLogService {

  @Inject
  ApiLogRepository apiLogRepository;

  public ApiLog create(ApiLog apiLog) {
    return apiLogRepository.save(apiLog);
  }

  public List<ApiLog> find(Predicate filter, Integer current, Integer pageSize) {
    return apiLogRepository.find(filter, current, pageSize);
  }

  public long count(Predicate filter) {
    return apiLogRepository.count(filter);
  }

  public List<LogStat> stat(Predicate filter) {
    return apiLogRepository.stat(filter);
  }

  public List<LogApiRank> ranking(Predicate filter) {
    return apiLogRepository.ranking(filter);
  }

  public void purgeOldLogs(int maxDays) {
    log.info("Purging old logs older than {} days", maxDays);
    LocalDateTime purgeDate = LocalDateTime.now().minusDays(maxDays);
    apiLogRepository.delete(apiLog.createdAt.lte(purgeDate));
  }
}
