package dev.flexmodel.domain.model.api;

import dev.flexmodel.codegen.entity.ApiRequestLog;
import dev.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiRequestLogRepository {

  List<ApiRequestLog> find(String projectId, Predicate filter, Integer page, Integer size);

  List<LogStat> stat(String projectId, Predicate filter, String fmt);

  List<LogApiRank> ranking(String projectId, Predicate filter);

  ApiRequestLog save(ApiRequestLog record);

  void delete(Predicate filter);

  long count(String projectId, Predicate filter);

}
