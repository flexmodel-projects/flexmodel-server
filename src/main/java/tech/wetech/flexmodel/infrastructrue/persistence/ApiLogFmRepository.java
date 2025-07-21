package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Projections;
import tech.wetech.flexmodel.codegen.dao.ApiRequestLogDAO;
import tech.wetech.flexmodel.codegen.entity.ApiRequestLog;
import tech.wetech.flexmodel.domain.model.api.ApiRequestLogRepository;
import tech.wetech.flexmodel.domain.model.api.LogApiRank;
import tech.wetech.flexmodel.domain.model.api.LogStat;
import tech.wetech.flexmodel.dsl.Predicate;

import java.util.List;

import static tech.wetech.flexmodel.Direction.DESC;
import static tech.wetech.flexmodel.Projections.dateFormat;
import static tech.wetech.flexmodel.Projections.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiLogFmRepository implements ApiRequestLogRepository {

  @Inject
  ApiRequestLogDAO apiRequestLogDAO;

  @Override
  public List<ApiRequestLog> find(Predicate filter, Integer current, Integer pageSize) {
    return apiRequestLogDAO.find(query -> {
      if (filter != null) {
        query.withFilter(filter);
      }
      query.withSort(sort -> sort.addOrder("id", DESC));
      if (current != null && pageSize != null) {
        query.withPage(current, pageSize);
      }
      return query;
    });
  }


  @Override
  public List<LogStat> stat(Predicate filter,String fmt) {
    return apiRequestLogDAO.find(query ->
      query
        .withProjection(projection -> projection
          .addField("date", dateFormat(field("created_at"), fmt))
          .addField("total", Projections.count(field("id")))
        )
        .withGroupBy(groupBy -> groupBy
          .addField("date")
        )
        .withFilter(filter), LogStat.class);
  }

  @Override
  public List<LogApiRank> ranking(Predicate filter) {
    return apiRequestLogDAO.find(query ->
      query
        .withProjection(projection -> projection
          .addField("name", field("path"))
          .addField("total", Projections.count(field("id")))
        )
        .withGroupBy(groupBy -> groupBy
          .addField("path")
        )
        .withPage(p -> p.setPageSize(20))
        .withSort(s -> s.addOrder("total", DESC))
        .withFilter(filter), LogApiRank.class);
  }

  @Override
  public ApiRequestLog save(ApiRequestLog record) {
    return apiRequestLogDAO.save(record);
  }

  @Override
  public void delete(Predicate unaryOperator) {
    apiRequestLogDAO.delete(unaryOperator);
  }

  @Override
  public long count(Predicate filter) {
    return apiRequestLogDAO.count(filter);
  }

}
