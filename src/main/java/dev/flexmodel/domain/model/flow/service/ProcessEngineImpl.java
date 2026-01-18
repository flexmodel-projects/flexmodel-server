package dev.flexmodel.domain.model.flow.service;

import dev.flexmodel.domain.model.flow.dto.param.*;
import dev.flexmodel.domain.model.flow.dto.result.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import dev.flexmodel.domain.model.flow.dto.param.*;
import dev.flexmodel.domain.model.flow.dto.result.*;
import dev.flexmodel.domain.model.flow.processor.DefinitionProcessor;
import dev.flexmodel.domain.model.flow.processor.RuntimeProcessor;

@Singleton
public class ProcessEngineImpl implements ProcessService {

  @Inject
  DefinitionProcessor definitionProcessor;

  @Inject
  RuntimeProcessor runtimeProcessor;

  @Override
  public CreateFlowResult createFlow(CreateFlowParam createFlowParam) {
    return definitionProcessor.create(createFlowParam);
  }

  @Override
  public UpdateFlowResult updateFlow(UpdateFlowParam updateFlowParam) {
    return definitionProcessor.update(updateFlowParam);
  }

  @Override
  public DeployFlowResult deployFlow(DeployFlowParam deployFlowParam) {
    return definitionProcessor.deploy(deployFlowParam);
  }

  @Override
  public FlowModuleResult getFlowModule(GetFlowModuleParam getFlowModuleParam) {
    return definitionProcessor.getFlowModule(getFlowModuleParam);
  }

  @Override
  public StartProcessResult startProcess(StartProcessParam startProcessParam) {
    return runtimeProcessor.startProcess(startProcessParam);
  }

  @Override
  public CommitTaskResult commitTask(CommitTaskParam commitTaskParam) {
    runtimeProcessor.checkIsSubFlowInstance(commitTaskParam.getProjectId(), commitTaskParam.getFlowInstanceId());
    return runtimeProcessor.commit(commitTaskParam);
  }

  @Override
  public RollbackTaskResult rollbackTask(RollbackTaskParam rollbackTaskParam) {
    runtimeProcessor.checkIsSubFlowInstance(rollbackTaskParam.getProjectId(), rollbackTaskParam.getFlowInstanceId());
    return runtimeProcessor.rollback(rollbackTaskParam);
  }

  @Override
  public TerminateResult terminateProcess(String projectId, String flowInstanceId) {
    runtimeProcessor.checkIsSubFlowInstance(projectId, flowInstanceId);
    return runtimeProcessor.terminateProcess(projectId, flowInstanceId, true);
  }

  @Override
  public TerminateResult terminateProcess(String projectId, String flowInstanceId, boolean effectiveForSubFlowInstance) {
    runtimeProcessor.checkIsSubFlowInstance(projectId, flowInstanceId);
    return runtimeProcessor.terminateProcess(projectId, flowInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public NodeInstanceListResult getHistoryUserTaskList(String projectId, String flowInstanceId) {
    return runtimeProcessor.getHistoryUserTaskList(projectId, flowInstanceId, true);
  }

  @Override
  public NodeInstanceListResult getHistoryUserTaskList(String projectId, String flowInstanceId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getHistoryUserTaskList(projectId, flowInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public ElementInstanceListResult getHistoryElementList(String projectId, String flowInstanceId) {
    return runtimeProcessor.getHistoryElementList(projectId, flowInstanceId, true);
  }

  @Override
  public ElementInstanceListResult getHistoryElementList(String projectId, String flowInstanceId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getHistoryElementList(projectId, flowInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public InstanceDataListResult getInstanceData(String projectId, String flowInstanceId) {
    return runtimeProcessor.getInstanceData(projectId, flowInstanceId, true);
  }

  @Override
  public InstanceDataListResult getInstanceData(String projectId, String flowInstanceId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getInstanceData(projectId, flowInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public NodeInstanceResult getNodeInstance(String projectId, String flowInstanceId, String nodeInstanceId) {
    return runtimeProcessor.getNodeInstance(projectId, flowInstanceId, nodeInstanceId, true);
  }

  @Override
  public NodeInstanceResult getNodeInstance(String projectId, String flowInstanceId, String nodeInstanceId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getNodeInstance(projectId, flowInstanceId, nodeInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public FlowInstanceResult getFlowInstance(String projectId, String flowInstanceId) {
    return runtimeProcessor.getFlowInstance(projectId, flowInstanceId);
  }

  @Override
  public void deleteFlow(String projectId, String flowModuleId) {
    definitionProcessor.delete(projectId, flowModuleId);
  }

  @Override
  public InstanceDataListResult getInstanceData(String projectId, String flowInstanceId, String instanceDataId) {
    return runtimeProcessor.getInstanceData(projectId, flowInstanceId, instanceDataId, true);
  }

  @Override
  public InstanceDataListResult getInstanceData(String projectId, String flowInstanceId, String instanceDataId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getInstanceData(projectId, flowInstanceId, instanceDataId, effectiveForSubFlowInstance);
  }
}
