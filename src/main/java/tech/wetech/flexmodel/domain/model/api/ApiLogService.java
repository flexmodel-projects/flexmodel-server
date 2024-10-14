package tech.wetech.flexmodel.domain.model.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.criterion.Example;

import java.util.List;
import java.util.function.UnaryOperator;

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

  public List<ApiLog> find(UnaryOperator<Example.Criteria> filter, Integer current, Integer pageSize) {
    return apiLogRepository.find(filter, current, pageSize);
  }

  public List<LogStat> stat(UnaryOperator<Example.Criteria> filter) {
    return apiLogRepository.stat(filter);
  }

}
