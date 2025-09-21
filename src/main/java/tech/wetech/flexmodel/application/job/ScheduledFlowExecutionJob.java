package tech.wetech.flexmodel.application.job;

import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tech.wetech.flexmodel.application.FlowApplicationService;
import tech.wetech.flexmodel.domain.model.flow.dto.param.StartProcessParam;
import tech.wetech.flexmodel.domain.model.flow.dto.result.StartProcessResult;

import java.util.Map;
import io.quarkus.arc.Arc;

/**
 * 流程执行任务
 * 用于 Quartz 定时调度执行流程实例
 *
 * @author cjbi
 */
@Slf4j
public class ScheduledFlowExecutionJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    Arc.container().requestContext().activate();
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
      StartProcessParam startProcessParam = new StartProcessParam();
      startProcessParam.setFlowModuleId(flowModuleId);
      startProcessParam.setVariables(Map.of());

      FlowApplicationService flowApplicationService = CDI.current().select(FlowApplicationService.class).get();
      // 启动流程实例
      StartProcessResult result = flowApplicationService.startProcess(startProcessParam);

      boolean success = result.getErrCode() == 0;

      // 将执行结果存储到上下文中，供监听器使用
      context.setResult(Map.of(
        "success", success,
        "errCode", result.getErrCode(),
        "errMsg", result.getErrMsg(),
        "flowInstanceId", result.getFlowInstanceId(),
        "flowModuleId", flowModuleId,
        "triggerId", triggerId
      ));

      log.info("定时流程任务执行完成: triggerId={}, flowInstanceId={}, success={}",
        triggerId, result.getFlowInstanceId(), success);

    } catch (Exception e) {
      log.error("执行定时流程任务失败", e);

      // 将错误信息存储到上下文中
      context.setResult(Map.of(
        "success", false,
        "error", e.getMessage(),
        "exception", e.getClass().getSimpleName()
      ));

      throw new JobExecutionException("执行定时流程任务失败", e);
    } finally {
      Arc.container().requestContext().deactivate();
    }
  }
}
