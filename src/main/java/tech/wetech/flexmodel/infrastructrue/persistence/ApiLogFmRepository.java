package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.dao.ApiLogDAO;
import tech.wetech.flexmodel.codegen.entity.ApiLog;
import tech.wetech.flexmodel.criterion.Example;
import tech.wetech.flexmodel.domain.model.api.ApiLogRepository;
import tech.wetech.flexmodel.domain.model.api.LogStat;

import java.util.List;
import java.util.function.UnaryOperator;

import static tech.wetech.flexmodel.Direction.DESC;
import static tech.wetech.flexmodel.Projections.*;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiLogFmRepository implements ApiLogRepository {

  @Inject
  ApiLogDAO apiLogDAO;

  @Override
  public List<ApiLog> find(UnaryOperator<Example.Criteria> filter, Integer current, Integer pageSize) {
    return apiLogDAO.find(query -> {
      if (filter != null) {
        query.setFilter(filter);
      }
      query.setSort(sort -> sort.addOrder("id", DESC));
      if (current != null && pageSize != null) {
        query.setPage(current, pageSize);
      }
      return query;
    });
  }


  @Override
  public List<LogStat> stat(UnaryOperator<Example.Criteria> filter) {
    return apiLogDAO.find(query ->
      query
        .setProjection(projection -> projection
          .addField("date", dateFormat(field("createdAt"), "yyyy-MM-dd hh:00:00"))
          .addField("total", count(field("id")))
        )
        .setGroupBy(groupBy -> groupBy
          .addField("date")
        )
        .setFilter(filter), LogStat.class);
  }

  @Override
  public ApiLog save(ApiLog record) {
    return apiLogDAO.save(record);
  }

  @Override
  public void delete(UnaryOperator<Example.Criteria> unaryOperator) {
    apiLogDAO.delete(unaryOperator);
  }

}
