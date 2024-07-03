package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import tech.wetech.flexmodel.domain.model.api.ApiLog;
import tech.wetech.flexmodel.domain.model.api.ApiLogRepository;
import tech.wetech.flexmodel.domain.model.api.LogStat;

import java.util.List;

import static tech.wetech.flexmodel.Direction.DESC;
import static tech.wetech.flexmodel.Projections.*;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiLogFmRepository extends BaseFmRepository<ApiLog, String> implements ApiLogRepository {

  @Override
  public List<ApiLog> find(String filter, Integer current, Integer pageSize) {
    return withSession(session -> {
      String entityName = getEntityName();
      return session.find(entityName, query -> {
        if (filter != null) {
          query.setFilter(filter);
        }
        query.setSort(sort -> sort.addOrder("id", DESC));
        if (pageSize != null) {
          query.setLimit(pageSize);
          if (current != null) {
            query.setOffset((current - 1) * pageSize);
          }
        }
        return query;
      }, ApiLog.class);
    });
  }


  @Override
  public List<LogStat> stat(String filter) {
    return withSession(session -> session
      .find(getEntityName(), query -> query
        .setProjection(projection -> projection
          .addField("date", dateFormat(field("createdAt" ), "yyyy-MM-dd hh:00:00" ))
          .addField("total", count(field("id" )))
        )
        .setGroupBy(groupBy -> groupBy
          .addField("date" )
        )
        .setFilter(filter), LogStat.class));
  }

}
