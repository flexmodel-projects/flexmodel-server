package tech.wetech.flexmodel.domain.model.api;

import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.criterion.Example;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public interface ApiLogRepository {

  List<ApiLog> find(UnaryOperator<Example.Criteria> filter, Integer current, Integer pageSize);

  List<LogStat> stat(UnaryOperator<Example.Criteria> filter);

  ApiLog save(ApiLog record);

  void delete(UnaryOperator<Example.Criteria> unaryOperator);
}
