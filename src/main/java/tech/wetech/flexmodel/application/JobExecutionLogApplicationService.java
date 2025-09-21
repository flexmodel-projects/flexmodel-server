package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.codegen.entity.JobExecutionLog;
import tech.wetech.flexmodel.domain.model.schedule.JobExecutionLogService;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.query.Predicate;

import java.time.LocalDateTime;
import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * 作业执行日志应用服务
 * 提供作业执行日志的查询和管理功能
 * 
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class JobExecutionLogApplicationService {

    @Inject
    JobExecutionLogService jobExecutionLogService;

    /**
     * 分页查询作业执行日志
     * 
     * @param triggerId 触发器ID（可选）
     * @param jobId 作业ID（可选）
     * @param status 执行状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param isSuccess 是否成功（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页结果
     */
    public PageDTO<JobExecutionLog> findPage(String triggerId, String jobId, String status,
                                           LocalDateTime startTime, LocalDateTime endTime,
                                           Boolean isSuccess, Integer page, Integer size) {
        List<JobExecutionLog> logs = jobExecutionLogService.findWithConditions(
            triggerId, jobId, status, startTime, endTime, isSuccess, page, size);
        long total = jobExecutionLogService.countWithConditions(
            triggerId, jobId, status, startTime, endTime, isSuccess);
        
        return new PageDTO<>(logs, total);
    }

    /**
     * 根据ID查询作业执行日志
     * 
     * @param id 日志ID
     * @return 作业执行日志
     */
    public JobExecutionLog findById(String id) {
        return jobExecutionLogService.findById(id);
    }

    /**
     * 根据触发器ID查询执行日志
     * 
     * @param triggerId 触发器ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public PageDTO<JobExecutionLog> findByTriggerId(String triggerId, Integer page, Integer size) {
        Predicate filter = field(JobExecutionLog::getTriggerId).eq(triggerId);
        List<JobExecutionLog> logs = jobExecutionLogService.find(filter, page, size);
        long total = jobExecutionLogService.count(filter);
        
        return new PageDTO<>(logs, total);
    }

    /**
     * 根据作业ID查询执行日志
     * 
     * @param jobId 作业ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public PageDTO<JobExecutionLog> findByJobId(String jobId, Integer page, Integer size) {
        Predicate filter = field(JobExecutionLog::getJobId).eq(jobId);
        List<JobExecutionLog> logs = jobExecutionLogService.find(filter, page, size);
        long total = jobExecutionLogService.count(filter);
        
        return new PageDTO<>(logs, total);
    }

    /**
     * 查询失败的执行日志
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public PageDTO<JobExecutionLog> findFailedLogs(Integer page, Integer size) {
        Predicate filter = field(JobExecutionLog::getIsSuccess).eq(false);
        List<JobExecutionLog> logs = jobExecutionLogService.find(filter, page, size);
        long total = jobExecutionLogService.count(filter);
        
        return new PageDTO<>(logs, total);
    }

    /**
     * 查询指定时间范围内的执行日志
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public PageDTO<JobExecutionLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime, 
                                                   Integer page, Integer size) {
        Predicate filter = field(JobExecutionLog::getStartTime).between(startTime, endTime);
        List<JobExecutionLog> logs = jobExecutionLogService.find(filter, page, size);
        long total = jobExecutionLogService.count(filter);
        
        return new PageDTO<>(logs, total);
    }

    /**
     * 统计指定触发器的执行次数
     * 
     * @param triggerId 触发器ID
     * @return 执行次数
     */
    public long countByTriggerId(String triggerId) {
        Predicate filter = field(JobExecutionLog::getTriggerId).eq(triggerId);
        return jobExecutionLogService.count(filter);
    }

    /**
     * 统计指定作业的执行次数
     * 
     * @param jobId 作业ID
     * @return 执行次数
     */
    public long countByJobId(String jobId) {
        Predicate filter = field(JobExecutionLog::getJobId).eq(jobId);
        return jobExecutionLogService.count(filter);
    }

    /**
     * 统计指定时间范围内的执行次数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行次数
     */
    public long countByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Predicate filter = field(JobExecutionLog::getStartTime).between(startTime, endTime);
        return jobExecutionLogService.count(filter);
    }

    /**
     * 统计成功执行次数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功执行次数
     */
    public long countSuccessByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Predicate filter = field(JobExecutionLog::getStartTime).between(startTime, endTime)
            .and(field(JobExecutionLog::getIsSuccess).eq(true));
        return jobExecutionLogService.count(filter);
    }

    /**
     * 统计失败执行次数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败执行次数
     */
    public long countFailedByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Predicate filter = field(JobExecutionLog::getStartTime).between(startTime, endTime)
            .and(field(JobExecutionLog::getIsSuccess).eq(false));
        return jobExecutionLogService.count(filter);
    }

    /**
     * 清理指定天数之前的日志
     * 
     * @param days 保留天数
     * @return 清理的记录数
     */
    public int purgeOldLogs(int days) {
        return jobExecutionLogService.purgeOldLogs(days);
    }

    /**
     * 获取执行统计信息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    public ExecutionStatistics getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        long total = countByTimeRange(startTime, endTime);
        long success = countSuccessByTimeRange(startTime, endTime);
        long failed = countFailedByTimeRange(startTime, endTime);
        
        return new ExecutionStatistics(total, success, failed);
    }

    /**
     * 执行统计信息
     */
    public static class ExecutionStatistics {
        private final long total;
        private final long success;
        private final long failed;
        private final double successRate;

        public ExecutionStatistics(long total, long success, long failed) {
            this.total = total;
            this.success = success;
            this.failed = failed;
            this.successRate = total > 0 ? (double) success / total * 100 : 0.0;
        }

        public long getTotal() {
            return total;
        }

        public long getSuccess() {
            return success;
        }

        public long getFailed() {
            return failed;
        }

        public double getSuccessRate() {
            return successRate;
        }
    }
}
