package tech.wetech.flexmodel.domain.model.api;

import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.dsl.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiLogRepository {

  List<ApiLog> find(Predicate filter, Integer current, Integer pageSize);

  List<LogStat> stat(Predicate filter);

  List<LogApiRank> ranking(Predicate filter);

  ApiLog save(ApiLog record);

  void delete(Predicate filter);

  long count(Predicate filter);

}
