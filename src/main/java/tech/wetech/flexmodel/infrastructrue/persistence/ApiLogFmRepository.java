package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiRequestLog;
import tech.wetech.flexmodel.domain.model.api.ApiRequestLogRepository;
import tech.wetech.flexmodel.domain.model.api.LogApiRank;
import tech.wetech.flexmodel.domain.model.api.LogStat;
import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.query.expr.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Query.dateFormat;
import static tech.wetech.flexmodel.query.Query.field;


/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiLogFmRepository implements ApiRequestLogRepository {

  @Inject
  Session session;

  @Override
  public List<ApiRequestLog> find(Predicate filter, Integer current, Integer pageSize) {
    return session.dsl().select()
      .from(ApiRequestLog.class)
      .where(filter)
      .orderBy("id", Direction.DESC)
      .page(current, pageSize)
      .execute();
  }


  @Override
  public List<LogStat> stat(Predicate filter, String fmt) {
    return session.dsl()
      .select(query -> query
        .field("date", dateFormat(field("created_at"), fmt))
        .field("total", Query.count(field("id"))))
      .from(ApiRequestLog.class)
      .where(filter)
      .groupBy("date")
      .execute(LogStat.class);
  }

  @Override
  public List<LogApiRank> ranking(Predicate filter) {
    return session.dsl()
      .select(query -> query
        .field("name", field("path"))
        .field("total", Query.count(field("id"))))
      .from(ApiRequestLog.class)
      .where(filter)
      .groupBy("path")
      .orderBy("total", Direction.DESC)
      .page(1, 20)
      .execute(LogApiRank.class);
  }

  @Override
  public ApiRequestLog save(ApiRequestLog record) {
    session.dsl()
      .mergeInto(ApiRequestLog.class)
      .values(record)
      .execute();
    return record;
  }

  @Override
  public void delete(Predicate unaryOperator) {
    session.dsl()
      .deleteFrom(ApiRequestLog.class)
      .where(unaryOperator)
      .execute();
  }

  @Override
  public long count(Predicate filter) {
    return session.dsl().select()
      .from(ApiRequestLog.class)
      .where(filter)
      .count();
  }

}
