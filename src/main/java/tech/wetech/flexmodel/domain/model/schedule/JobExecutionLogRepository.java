package tech.wetech.flexmodel.domain.model.schedule;

import tech.wetech.flexmodel.codegen.entity.JobExecutionLog;
import tech.wetech.flexmodel.query.Predicate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业执行日志仓储接口
 *
 * @author cjbi
 */
public interface JobExecutionLogRepository {

    /**
     * 根据ID查找作业执行日志
     *
     * @param id 日志ID
     * @return 作业执行日志
     */
    JobExecutionLog findById(String id);

    /**
     * 保存作业执行日志
     *
     * @param jobExecutionLog 作业执行日志
     * @return 保存后的作业执行日志
     */
    JobExecutionLog save(JobExecutionLog jobExecutionLog);

    /**
     * 根据条件删除作业执行日志
     *
     * @param filter 删除条件
     */
    void delete(Predicate filter);

    /**
     * 分页查询作业执行日志
     *
     * @param filter 查询条件
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 作业执行日志列表
     */
    List<JobExecutionLog> find(Predicate filter, Integer page, Integer size);

    /**
     * 统计作业执行日志数量
     *
     * @param filter 查询条件
     * @return 日志数量
     */
    long count(Predicate filter);

    /**
     * 清理指定天数之前的日志
     *
     * @param days 保留天数
     * @return 清理的记录数
     */
    int purgeOldLogs(int days);
}
