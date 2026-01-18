package dev.flexmodel.application;

import dev.flexmodel.application.dto.*;
import dev.flexmodel.domain.model.flow.dto.param.*;
import dev.flexmodel.domain.model.flow.dto.result.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.application.dto.*;
import dev.flexmodel.codegen.entity.FlowDefinition;
import dev.flexmodel.codegen.entity.FlowDeployment;
import dev.flexmodel.codegen.entity.FlowInstance;
import dev.flexmodel.domain.model.flow.dto.bo.ElementInstance;
import dev.flexmodel.domain.model.flow.dto.bo.NodeInstance;
import dev.flexmodel.domain.model.flow.dto.param.*;
import dev.flexmodel.domain.model.flow.dto.result.*;
import dev.flexmodel.domain.model.flow.service.FlowDefinitionService;
import dev.flexmodel.domain.model.flow.service.FlowDeploymentService;
import dev.flexmodel.domain.model.flow.service.FlowInstanceService;
import dev.flexmodel.domain.model.flow.service.ProcessService;
import dev.flexmodel.domain.model.flow.shared.common.FlowDeploymentStatus;
import dev.flexmodel.query.Expressions;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.shared.utils.JsonUtils;
import dev.flexmodel.shared.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

  public List<NodeInstance> getHistoryUserTaskList(String projectId, String flowInstanceId, boolean effectiveForSubFlowInstance) {
    return processService.getHistoryUserTaskList(projectId, flowInstanceId, effectiveForSubFlowInstance).getNodeInstanceList();
  }

  public List<ElementInstance> getHistoryElementList(String projectId, String flowInstanceId) {
    return processService.getHistoryElementList(projectId, flowInstanceId).getElementInstanceList();
  }

  /**
   * 获取流程模块列表
   */
  public PageDTO<FlowModuleResponse> findFlowModuleList(FlowModuleListRequest request) {
    log.info("获取流程模块列表，参数: {}", request);
    Predicate predicate = Expressions.field(FlowDefinition::getIsDeleted).eq(false);
    if (StringUtils.isNotBlank(request.getFlowModuleId())) {
      predicate = predicate.and(Expressions.field(FlowDefinition::getFlowModuleId).eq(request.getFlowModuleId()));
    }
    if (StringUtils.isNotBlank(request.getFlowKey())) {
      predicate = predicate.and(Expressions.field(FlowDefinition::getFlowKey).eq(request.getFlowKey()));
    }
    if (StringUtils.isNotBlank(request.getFlowName())) {
      predicate = predicate.and(Expressions.field(FlowDefinition::getFlowName).contains(request.getFlowName()));
    }
    long count = flowDefinitionService.count(request.getProjectId(), predicate);
    if (count == 0) {
      return PageDTO.empty();
    }

    List<FlowDefinition> list = flowDefinitionService.find(request.getProjectId(), predicate, request.getPage(), request.getSize());
    List<FlowModuleResponse> flowModuleList = new ArrayList<>();
    for (FlowDefinition entity : list) {
      FlowModuleResponse response = new FlowModuleResponse(entity);
      long deploymentCount = flowDeploymentService.count(request.getProjectId(), Expressions.field(
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
  public PageDTO<FlowInstanceResponse> findFlowInstanceList(FlowInstanceListRequest request) {
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
    long count = flowInstanceService.count(request.getProjectId(), predicate);
    if (count == 0) {
      return PageDTO.empty();
    }
    List<FlowInstanceResponse> list = flowInstanceService.find(request.getProjectId(), predicate, request.getPage(), request.getSize()).stream()
      .map(entity -> {
        FlowInstanceResponse response = JsonUtils.getInstance().convertValue(entity, FlowInstanceResponse.class);
        FlowDeployment flowDeployment = flowDeploymentService.findByFlowDeployId(request.getProjectId(), entity.getFlowDeployId());
        if (flowDeployment != null) {
          response.setFlowName(flowDeployment.getFlowName());
          response.setFlowKey(flowDeployment.getFlowKey());
        }
        return response;
      }).toList();
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
   * 更新流程
   */
  public UpdateFlowResult updateFlow(UpdateFlowParam updateFlowParam) {
    log.info("更新流程，流程模块ID: {}", updateFlowParam.getFlowModuleId());
    return processService.updateFlow(updateFlowParam);
  }

  public void deleteFlow(String projectId, String flowModuleId) {
    log.info("删除流程，流程模块ID: {}", flowModuleId);
    processService.deleteFlow(projectId, flowModuleId);
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
  public TerminateResult terminateProcess(String projectId, String flowInstanceId, boolean effectiveForSubFlowInstance) {
    log.info("终止流程实例: {}, 对子流程实例生效: {}", flowInstanceId, effectiveForSubFlowInstance);
    if (StringUtils.isBlank(flowInstanceId)) {
      throw new IllegalArgumentException("流程实例ID不能为空");
    }
    return processService.terminateProcess(projectId, flowInstanceId, effectiveForSubFlowInstance);
  }

  /**
   * 获取流程实例信息
   */
  public FlowInstance findFlowInstance(String projectId, String flowInstanceId) {
    log.info("获取流程实例信息: {}", flowInstanceId);
    if (StringUtils.isBlank(flowInstanceId)) {
      throw new IllegalArgumentException("流程实例ID不能为空");
    }
    return flowInstanceService.findById(projectId, flowInstanceId);
  }

  public Map<String, Object> getInstanceData(String flowInstanceId, String instanceDataId, String dataId) {
    return null;
  }
}
