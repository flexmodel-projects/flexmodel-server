package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.JobExecutionLog;
import tech.wetech.flexmodel.domain.model.schedule.JobExecutionLogRepository;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.time.LocalDateTime;
import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * 作业执行日志仓储实现
 *
 * @author cjbi
 */
@ApplicationScoped
@ActivateRequestContext
public class JobExecutionLogFmRepository implements JobExecutionLogRepository {

  @Inject
  Session session;

  @Override
  public JobExecutionLog findById(String id) {
    return session.dsl()
      .selectFrom(JobExecutionLog.class)
      .where(field(JobExecutionLog::getId).eq(id))
      .executeOne();
  }

  @Override
  public JobExecutionLog save(JobExecutionLog jobExecutionLog) {
    session.dsl()
      .mergeInto(JobExecutionLog.class)
      .values(jobExecutionLog)
      .execute();
    return jobExecutionLog;
  }

  @Override
  public void delete(Predicate filter) {
    session.dsl()
      .deleteFrom(JobExecutionLog.class)
      .where(filter)
      .execute();
  }

  @Override
  public List<JobExecutionLog> find(Predicate filter, Integer page, Integer size) {
    var query = session.dsl()
      .selectFrom(JobExecutionLog.class)
      .where(filter)
      .page(page, size)
      .orderByDesc(JobExecutionLog::getStartTime);

    return query.execute();
  }

  @Override
  public long count(Predicate filter) {
    return session.dsl()
      .selectFrom(JobExecutionLog.class)
      .where(filter)
      .count();
  }

  @Override
  public int purgeOldLogs(int days) {
    LocalDateTime purgeDate = LocalDateTime.now().minusDays(days);
    Predicate filter = field(JobExecutionLog::getCreatedAt).lte(purgeDate);

    // 先统计要删除的记录数
    long count = count(filter);

    // 执行删除
    delete(filter);

    return (int) count;
  }
}
