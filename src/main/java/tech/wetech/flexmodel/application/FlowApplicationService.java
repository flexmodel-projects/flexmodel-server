package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.domain.model.flow.dto.param.*;
import tech.wetech.flexmodel.domain.model.flow.dto.result.*;
import tech.wetech.flexmodel.domain.model.flow.service.ProcessService;
import tech.wetech.flexmodel.shared.utils.StringUtils;

/**
 * 流程编排应用服务
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class FlowApplicationService {

  @Inject
  ProcessService processService;

  /**
   * 创建流程
   */
  public CreateFlowResult createFlow(CreateFlowParam createFlowParam) {
    log.info("创建流程: {}", createFlowParam.getFlowName());
    validateCreateFlowParam(createFlowParam);
    return processService.createFlow(createFlowParam);
  }

  /**
   * 部署流程
   */
  public DeployFlowResult deployFlow(DeployFlowParam deployFlowParam) {
    log.info("部署流程，流程模块ID: {}", deployFlowParam.getFlowModuleId());
    validateDeployFlowParam(deployFlowParam);
    return processService.deployFlow(deployFlowParam);
  }

  /**
   * 获取流程模块信息
   */
  public FlowModuleResult getFlowModule(GetFlowModuleParam getFlowModuleParam) {
    log.info("获取流程模块信息，流程模块ID: {}, 流程部署ID: {}",
      getFlowModuleParam.getFlowModuleId(), getFlowModuleParam.getFlowDeployId());
    return processService.getFlowModule(getFlowModuleParam);
  }

  /**
   * 启动流程实例
   */
  public StartProcessResult startProcess(StartProcessParam startProcessParam) {
    log.info("启动流程实例，流程模块ID: {}, 流程部署ID: {}",
      startProcessParam.getFlowModuleId(), startProcessParam.getFlowDeployId());
    validateStartProcessParam(startProcessParam);
    return processService.startProcess(startProcessParam);
  }

  /**
   * 提交任务
   */
  public CommitTaskResult commitTask(CommitTaskParam commitTaskParam) {
    log.info("提交任务，流程实例ID: {}, 任务实例ID: {}",
      commitTaskParam.getFlowInstanceId(), commitTaskParam.getTaskInstanceId());
    validateCommitTaskParam(commitTaskParam);
    return processService.commitTask(commitTaskParam);
  }

  /**
   * 回滚任务
   */
  public RollbackTaskResult rollbackTask(RollbackTaskParam rollbackTaskParam) {
    log.info("回滚任务，流程实例ID: {}, 任务实例ID: {}",
      rollbackTaskParam.getFlowInstanceId(), rollbackTaskParam.getTaskInstanceId());
    validateRollbackTaskParam(rollbackTaskParam);
    return processService.rollbackTask(rollbackTaskParam);
  }

  /**
   * 终止流程实例
   */
  public TerminateResult terminateProcess(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    log.info("终止流程实例: {}, 对子流程实例生效: {}", flowInstanceId, effectiveForSubFlowInstance);
    if (StringUtils.isBlank(flowInstanceId)) {
      throw new IllegalArgumentException("流程实例ID不能为空");
    }
    return processService.terminateProcess(flowInstanceId, effectiveForSubFlowInstance);
  }

  /**
   * 获取流程实例信息
   */
  public FlowInstanceResult getFlowInstance(String flowInstanceId) {
    log.info("获取流程实例信息: {}", flowInstanceId);
    if (StringUtils.isBlank(flowInstanceId)) {
      throw new IllegalArgumentException("流程实例ID不能为空");
    }
    return processService.getFlowInstance(flowInstanceId);
  }

  /**
   * 验证创建流程参数
   */
  private void validateCreateFlowParam(CreateFlowParam createFlowParam) {
    if (createFlowParam == null) {
      throw new IllegalArgumentException("创建流程参数不能为空");
    }
    if (StringUtils.isBlank(createFlowParam.getFlowKey())) {
      throw new IllegalArgumentException("流程键不能为空");
    }
    if (StringUtils.isBlank(createFlowParam.getFlowName())) {
      throw new IllegalArgumentException("流程名称不能为空");
    }
  }

  /**
   * 验证部署流程参数
   */
  private void validateDeployFlowParam(DeployFlowParam deployFlowParam) {
    if (deployFlowParam == null) {
      throw new IllegalArgumentException("部署流程参数不能为空");
    }
    if (StringUtils.isBlank(deployFlowParam.getFlowModuleId())) {
      throw new IllegalArgumentException("流程模块ID不能为空");
    }
  }

  /**
   * 验证启动流程参数
   */
  private void validateStartProcessParam(StartProcessParam startProcessParam) {
    if (startProcessParam == null) {
      throw new IllegalArgumentException("启动流程参数不能为空");
    }
    if (StringUtils.isBlank(startProcessParam.getFlowModuleId()) && StringUtils.isBlank(startProcessParam.getFlowDeployId())) {
      throw new IllegalArgumentException("流程模块ID或流程部署ID不能同时为空");
    }
  }

  /**
   * 验证提交任务参数
   */
  private void validateCommitTaskParam(CommitTaskParam commitTaskParam) {
    if (commitTaskParam == null) {
      throw new IllegalArgumentException("提交任务参数不能为空");
    }
    if (StringUtils.isBlank(commitTaskParam.getFlowInstanceId())) {
      throw new IllegalArgumentException("流程实例ID不能为空");
    }
    if (StringUtils.isBlank(commitTaskParam.getTaskInstanceId())) {
      throw new IllegalArgumentException("任务实例ID不能为空");
    }
  }

  /**
   * 验证回滚任务参数
   */
  private void validateRollbackTaskParam(RollbackTaskParam rollbackTaskParam) {
    if (rollbackTaskParam == null) {
      throw new IllegalArgumentException("回滚任务参数不能为空");
    }
    if (StringUtils.isBlank(rollbackTaskParam.getFlowInstanceId())) {
      throw new IllegalArgumentException("流程实例ID不能为空");
    }
    if (StringUtils.isBlank(rollbackTaskParam.getTaskInstanceId())) {
      throw new IllegalArgumentException("任务实例ID不能为空");
    }
  }
}
