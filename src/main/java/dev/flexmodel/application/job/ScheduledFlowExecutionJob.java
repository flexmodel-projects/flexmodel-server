package dev.flexmodel.application.job;

import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import dev.flexmodel.domain.model.flow.dto.StartProcessParamEvent;
import dev.flexmodel.shared.SessionContextHolder;

import java.util.Map;

/**
 * 流程执行任务
 * 用于 Quartz 定时调度执行流程实例
 *
 * @author cjbi
 */
@Slf4j
public class ScheduledFlowExecutionJob implements Job {

  @Inject
  EventBus eventBus;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      // 从 JobDataMap 中获取流程执行参数
      String flowModuleId = context.getJobDetail().getJobDataMap().getString("jobId");
      String triggerId = context.getJobDetail().getJobDataMap().getString("triggerId");

      if (flowModuleId == null) {
        log.error("流程执行任务缺少必要参数: flowModuleId=null");
        throw new JobExecutionException("流程执行任务缺少必要参数");
      }

      log.info("开始执行定时流程任务: triggerId={}, flowModuleId={}",
        triggerId, flowModuleId);

      // 构建启动流程参数
      StartProcessParamEvent startProcessParam = new StartProcessParamEvent();
      startProcessParam.setFlowModuleId(flowModuleId);
      startProcessParam.setVariables(Map.of());
      startProcessParam.setProjectId(SessionContextHolder.getProjectId());
      startProcessParam.setUserId(SessionContextHolder.getUserId());

      // 启动流程实例
      eventBus.send("flow.start", startProcessParam);

      // 将执行结果存储到上下文中，供监听器使用
      context.setResult(Map.of(
        "success", true,
        "errMsg", "",
        "flowModuleId", flowModuleId,
        "triggerId", triggerId
      ));

    } catch (Exception e) {
      log.error("执行定时流程任务失败", e);

      // 将错误信息存储到上下文中
      context.setResult(Map.of(
        "success", false,
        "error", e.getMessage(),
        "exception", e.getClass().getSimpleName()
      ));

      throw new JobExecutionException("执行定时流程任务失败", e);
    }
  }
}
