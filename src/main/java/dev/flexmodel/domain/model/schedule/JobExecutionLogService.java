package dev.flexmodel.domain.model.schedule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.codegen.entity.JobExecutionLog;
import dev.flexmodel.query.Expressions;
import dev.flexmodel.query.Predicate;

import java.time.LocalDateTime;
import java.util.List;

import static dev.flexmodel.query.Expressions.field;

/**
 * 作业执行日志服务
 *
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
@ActivateRequestContext
public class JobExecutionLogService {

    @Inject
    JobExecutionLogRepository jobExecutionLogRepository;

    /**
     * 创建作业执行日志
     *
     * @param jobExecutionLog 作业执行日志
     * @return 创建后的作业执行日志
     */
    public JobExecutionLog create(JobExecutionLog jobExecutionLog) {
        log.debug("创建作业执行日志: triggerId={}, jobId={}",
            jobExecutionLog.getTriggerId(), jobExecutionLog.getJobId());
        return jobExecutionLogRepository.save(jobExecutionLog.getProjectId(), jobExecutionLog);
    }

    /**
     * 根据ID查找作业执行日志
     *
     * @param id 日志ID
     * @return 作业执行日志
     */
    public JobExecutionLog findById(String id) {
        return jobExecutionLogRepository.findById(id);
    }

    /**
     * 更新作业执行日志
     *
     * @param jobExecutionLog 作业执行日志
     * @return 更新后的作业执行日志
     */
    public JobExecutionLog update(JobExecutionLog jobExecutionLog) {
        log.debug("更新作业执行日志: id={}, status={}",
            jobExecutionLog.getId(), jobExecutionLog.getExecutionStatus());
        return jobExecutionLogRepository.save(jobExecutionLog.getProjectId(), jobExecutionLog);
    }

    /**
     * 分页查询作业执行日志
     *
     * @param filter 查询条件
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 作业执行日志列表
     */
    public List<JobExecutionLog> find(Predicate filter, Integer page, Integer size) {
        return jobExecutionLogRepository.find("", filter, page, size);
    }

    /**
     * 统计作业执行日志数量
     *
     * @param filter 查询条件
     * @return 日志数量
     */
    public long count(Predicate filter) {
        return jobExecutionLogRepository.count("", filter);
    }

    /**
     * 记录作业开始执行
     *
     * @param triggerId 触发器ID
     * @param jobId 作业ID
     * @param jobGroup 作业分组
     * @param jobType 作业类型
     * @param jobName 作业名称
     * @param schedulerName 调度器名称
     * @param instanceName 实例名称
     * @param firedTime 实际触发时间
     * @param scheduledTime 计划执行时间
     * @param inputData 输入数据
     * @param projectId 租户ID
     * @return 创建的作业执行日志
     */
    public JobExecutionLog recordJobStart(String triggerId, String jobId, String jobGroup, String jobType,
                                         String jobName, String schedulerName, String instanceName,
                                         Long firedTime, Long scheduledTime, Object inputData, String projectId) {
        JobExecutionLog log = new JobExecutionLog();
        log.setTriggerId(triggerId);
        log.setJobId(jobId);
        log.setJobGroup(jobGroup);
        log.setJobType(jobType);
        log.setJobName(jobName);
        log.setExecutionStatus("RUNNING");
        log.setStartTime(LocalDateTime.now());
        log.setIsSuccess(false);
        log.setSchedulerName(schedulerName);
        log.setInstanceName(instanceName);
        log.setFiredTime(firedTime);
        log.setScheduledTime(scheduledTime);
        log.setInputData(inputData);
        log.setProjectId(projectId);
        log.setRetryCount(0);
        log.setMaxRetryCount(0);

        return create(log);
    }

    /**
     * 记录作业执行成功
     *
     * @param logId 日志ID
     * @param outputData 输出数据
     * @param executionDuration 执行时长（毫秒）
     */
    public void recordJobSuccess(String logId, Object outputData, Long executionDuration) {
        JobExecutionLog log = findById(logId);
        if (log != null) {
            log.setExecutionStatus("SUCCESS");
            log.setIsSuccess(true);
            log.setEndTime(LocalDateTime.now());
            log.setExecutionDuration(executionDuration);
            log.setOutputData(outputData);
            update(log);
        }
    }

    /**
     * 记录作业执行失败
     *
     * @param logId 日志ID
     * @param errorMessage 错误信息
     * @param errorStackTrace 错误堆栈
     * @param executionDuration 执行时长（毫秒）
     */
    public void recordJobFailure( String logId, String errorMessage, Object errorStackTrace, Long executionDuration) {
        JobExecutionLog log = findById(logId);
        if (log != null) {
            log.setExecutionStatus("FAILED");
            log.setIsSuccess(false);
            log.setEndTime(LocalDateTime.now());
            log.setExecutionDuration(executionDuration);
            log.setErrorMessage(errorMessage);
            log.setErrorStackTrace(errorStackTrace);
            update(log);
        }
    }

    /**
     * 清理指定天数之前的日志
     *
     * @param days 保留天数
     * @return 清理的记录数
     */
    public int purgeOldLogs(int days) {
        log.info("清理 {} 天之前的作业执行日志", days);
        LocalDateTime purgeDate = LocalDateTime.now().minusDays(days);
        Predicate filter = field(JobExecutionLog::getCreatedAt).lte(purgeDate);

        jobExecutionLogRepository.delete("", filter);

        return 0;
    }

    /**
     * 根据多个条件查询日志
     *
     * @param triggerId 触发器ID（可选）
     * @param jobId 作业ID（可选）
     * @param status 执行状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param isSuccess 是否成功（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 执行日志列表
     */
    public List<JobExecutionLog> findWithConditions(String triggerId, String jobId, String status,
                                                   LocalDateTime startTime, LocalDateTime endTime,
                                                   Boolean isSuccess, Integer page, Integer size) {
        Predicate filter = Expressions.TRUE;

        if (triggerId != null && !triggerId.trim().isEmpty()) {
            filter = filter.and(field(JobExecutionLog::getTriggerId).eq(triggerId));
        }

        if (jobId != null && !jobId.trim().isEmpty()) {
            filter = filter.and(field(JobExecutionLog::getJobId).eq(jobId));
        }

        if (status != null && !status.trim().isEmpty()) {
            filter = filter.and(field(JobExecutionLog::getExecutionStatus).eq(status));
        }

        if (startTime != null) {
            filter = filter.and(field(JobExecutionLog::getStartTime).gte(startTime));
        }

        if (endTime != null) {
            filter = filter.and(field(JobExecutionLog::getStartTime).lte(endTime));
        }

        if (isSuccess != null) {
            filter = filter.and(field(JobExecutionLog::getIsSuccess).eq(isSuccess));
        }

        return find(filter, page, size);
    }

    /**
     * 统计指定条件下的日志数量
     *
     * @param triggerId 触发器ID（可选）
     * @param jobId 作业ID（可选）
     * @param status 执行状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param isSuccess 是否成功（可选）
     * @return 日志数量
     */
    public long countWithConditions(String triggerId, String jobId, String status,
                                   LocalDateTime startTime, LocalDateTime endTime, Boolean isSuccess) {
        Predicate filter = Expressions.TRUE;

        if (triggerId != null && !triggerId.trim().isEmpty()) {
            filter = filter.and(field(JobExecutionLog::getTriggerId).eq(triggerId));
        }

        if (jobId != null && !jobId.trim().isEmpty()) {
            filter = filter.and(field(JobExecutionLog::getJobId).eq(jobId));
        }

        if (status != null && !status.trim().isEmpty()) {
            filter = filter.and(field(JobExecutionLog::getExecutionStatus).eq(status));
        }

        if (startTime != null) {
            filter = filter.and(field(JobExecutionLog::getStartTime).gte(startTime));
        }

        if (endTime != null) {
            filter = filter.and(field(JobExecutionLog::getStartTime).lte(endTime));
        }

        if (isSuccess != null) {
            filter = filter.and(field(JobExecutionLog::getIsSuccess).eq(isSuccess));
        }

        return count(filter);
    }
}
