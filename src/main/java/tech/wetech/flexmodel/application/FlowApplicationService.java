package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.application.dto.*;
import tech.wetech.flexmodel.codegen.entity.FlowDefinition;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.codegen.entity.FlowInstance;
import tech.wetech.flexmodel.domain.model.flow.dto.param.*;
import tech.wetech.flexmodel.domain.model.flow.dto.result.*;
import tech.wetech.flexmodel.domain.model.flow.service.FlowDefinitionService;
import tech.wetech.flexmodel.domain.model.flow.service.FlowDeploymentService;
import tech.wetech.flexmodel.domain.model.flow.service.FlowInstanceService;
import tech.wetech.flexmodel.domain.model.flow.service.ProcessService;
import tech.wetech.flexmodel.domain.model.flow.shared.common.FlowDeploymentStatus;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程编排应用服务
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class FlowApplicationService {

  @Inject
  ProcessService processService;

  @Inject
  FlowInstanceService flowInstanceService;

  @Inject
  FlowDefinitionService flowDefinitionService;

  @Inject
  FlowDeploymentService flowDeploymentService;

  /**
   * 获取流程模块列表
   */
  public PageDTO<FlowModuleResponse> findFlowModuleList(FlowModuleListRequest request) {
    log.info("获取流程模块列表，参数: {}", request);
    Predicate predicate = Expressions.TRUE;
    if (StringUtils.isNotBlank(request.getFlowModuleId())) {
      predicate = predicate.and(Expressions.field(FlowDefinition::getFlowModuleId).eq(request.getFlowModuleId()));
    }
    if (StringUtils.isNotBlank(request.getFlowName())) {
      predicate = predicate.and(Expressions.field(FlowDefinition::getFlowName).contains(request.getFlowName()));
    }
    long count = flowDefinitionService.count(predicate);
    if (count == 0) {
      return PageDTO.empty();
    }

    List<FlowDefinition> list = flowDefinitionService.find(predicate, request.getPage(), request.getSize());
    List<FlowModuleResponse> flowModuleList = new ArrayList<>();
    for (FlowDefinition entity : list) {
      FlowModuleResponse response = new FlowModuleResponse(entity);
      long deploymentCount = flowDeploymentService.count(Expressions.field(
          FlowDeployment::getFlowModuleId).eq(entity.getFlowModuleId())
        .and(Expressions.field(FlowDeployment::getStatus).eq(FlowDeploymentStatus.DEPLOYED))
      );
      if (deploymentCount >= 1) {
        //4 已发布
        response.setStatus(FlowModuleStatusEnum.PUBLISHED.getValue());
      }
      flowModuleList.add(response);
    }
    return new PageDTO<>(flowModuleList, count);
  }

  /**
   * 获取流程实例列表
   */
  public PageDTO<FlowInstance> findFlowInstanceList(FlowInstanceListRequest request) {
    log.info("获取流程实例列表，参数: {}", request);
    Predicate predicate = Expressions.TRUE;
    if (StringUtils.isNotBlank(request.getFlowInstanceId())) {
      predicate = predicate.and(Expressions.field(FlowInstance::getFlowInstanceId).eq(request.getFlowInstanceId()));
    }
    if (StringUtils.isNotBlank(request.getFlowModuleId())) {
      predicate = predicate.and(Expressions.field(FlowInstance::getFlowModuleId).eq(request.getFlowModuleId()));
    }
    if (StringUtils.isNotBlank(request.getFlowDeployId())) {
      predicate = predicate.and(Expressions.field(FlowInstance::getFlowDeployId).eq(request.getFlowDeployId()));
    }
    if (request.getStatus() != null) {
      predicate = predicate.and(Expressions.field(FlowInstance::getStatus).eq(request.getStatus()));
    }
    long count = flowInstanceService.count(predicate);
    if (count == 0) {
      return PageDTO.empty();
    }
    List<FlowInstance> list = flowInstanceService.find(predicate, request.getPage(), request.getSize());
    return new PageDTO<>(list, count);
  }

  /**
   * 创建流程
   */
  public CreateFlowResult createFlow(CreateFlowParam createFlowParam) {
    log.info("创建流程: {}", createFlowParam.getFlowName());
    return processService.createFlow(createFlowParam);
  }

  /**
   * 部署流程
   */
  public DeployFlowResult deployFlow(DeployFlowParam deployFlowParam) {
    log.info("部署流程，流程模块ID: {}", deployFlowParam.getFlowModuleId());
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
    return processService.startProcess(startProcessParam);
  }

  /**
   * 提交任务
   */
  public CommitTaskResult commitTask(CommitTaskParam commitTaskParam) {
    log.info("提交任务，流程实例ID: {}, 任务实例ID: {}",
      commitTaskParam.getFlowInstanceId(), commitTaskParam.getTaskInstanceId());
    return processService.commitTask(commitTaskParam);
  }

  /**
   * 回滚任务
   */
  public RollbackTaskResult rollbackTask(RollbackTaskParam rollbackTaskParam) {
    log.info("回滚任务，流程实例ID: {}, 任务实例ID: {}",
      rollbackTaskParam.getFlowInstanceId(), rollbackTaskParam.getTaskInstanceId());
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
  public FlowInstance findFlowInstance(String flowInstanceId) {
    log.info("获取流程实例信息: {}", flowInstanceId);
    if (StringUtils.isBlank(flowInstanceId)) {
      throw new IllegalArgumentException("流程实例ID不能为空");
    }
    return flowInstanceService.findById(flowInstanceId);
  }

}
