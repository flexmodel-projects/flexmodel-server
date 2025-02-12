package tech.wetech.flexmodel.domain.model.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.criterion.Example;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.UnaryOperator;

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

  public List<ApiLog> find(UnaryOperator<Example.Criteria> filter, Integer current, Integer pageSize) {
    return apiLogRepository.find(filter, current, pageSize);
  }

  public long count(UnaryOperator<Example.Criteria> filter) {
    return apiLogRepository.count(filter);
  }

  public List<LogStat> stat(UnaryOperator<Example.Criteria> filter) {
    return apiLogRepository.stat(filter);
  }

  public List<LogApiRank> ranking(UnaryOperator<Example.Criteria> filter) {
    return apiLogRepository.ranking(filter);
  }

  public void purgeOldLogs(int maxDays) {
    log.info("Purging old logs older than {} days", maxDays);
    LocalDateTime purgeDate = LocalDateTime.now().minusDays(maxDays);
    apiLogRepository.delete(f -> f.lessThanOrEqualTo("createdAt", purgeDate));
  }
}
