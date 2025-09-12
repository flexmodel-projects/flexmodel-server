package tech.wetech.flexmodel.domain.model.api;

import tech.wetech.flexmodel.codegen.entity.ApiRequestLog;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiRequestLogRepository {

  List<ApiRequestLog> find(Predicate filter, Integer page, Integer size);

  List<LogStat> stat(Predicate filter, String fmt);

  List<LogApiRank> ranking(Predicate filter);

  ApiRequestLog save(ApiRequestLog record);

  void delete(Predicate filter);

  long count(Predicate filter);

}
