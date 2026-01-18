package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.JobExecutionLog;
import dev.flexmodel.domain.model.schedule.JobExecutionLogRepository;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.session.Session;

import java.time.LocalDateTime;
import java.util.List;

import static dev.flexmodel.query.Expressions.field;

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
  public JobExecutionLog save(String projectId, JobExecutionLog jobExecutionLog) {
    session.dsl()
      .mergeInto(JobExecutionLog.class)
      .values(jobExecutionLog)
      .execute();
    return jobExecutionLog;
  }

  @Override
  public void delete(String projectId, Predicate filter) {
    session.dsl()
      .deleteFrom(JobExecutionLog.class)
      .where(field(JobExecutionLog::getProjectId).eq(projectId).and(filter))
      .execute();
  }

  @Override
  public List<JobExecutionLog> find(String projectId, Predicate filter, Integer page, Integer size) {
    var query = session.dsl()
      .selectFrom(JobExecutionLog.class)
      .where(field(JobExecutionLog::getProjectId).eq(projectId).and(filter))
      .page(page, size)
      .orderByDesc(JobExecutionLog::getStartTime);

    return query.execute();
  }

  @Override
  public long count(String projectId, Predicate filter) {
    return session.dsl()
      .selectFrom(JobExecutionLog.class)
      .where(field(JobExecutionLog::getProjectId).eq(projectId).and(filter))
      .count();
  }

  @Override
  public int purgeOldLogs(String projectId, int days) {
    LocalDateTime purgeDate = LocalDateTime.now().minusDays(days);
    Predicate filter = field(JobExecutionLog::getCreatedAt).lte(purgeDate);

    // 先统计要删除的记录数
    long count = count(projectId, filter);

    // 执行删除
    delete(projectId, filter);

    return (int) count;
  }
}
