package tech.wetech.flexmodel.domain.model.flow.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.domain.model.flow.dto.param.*;
import tech.wetech.flexmodel.domain.model.flow.dto.result.*;
import tech.wetech.flexmodel.domain.model.flow.processor.DefinitionProcessor;
import tech.wetech.flexmodel.domain.model.flow.processor.RuntimeProcessor;

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
    runtimeProcessor.checkIsSubFlowInstance(commitTaskParam.getFlowInstanceId());
    return runtimeProcessor.commit(commitTaskParam);
  }

  @Override
  public RollbackTaskResult rollbackTask(RollbackTaskParam rollbackTaskParam) {
    runtimeProcessor.checkIsSubFlowInstance(rollbackTaskParam.getFlowInstanceId());
    return runtimeProcessor.rollback(rollbackTaskParam);
  }

  @Override
  public TerminateResult terminateProcess(String flowInstanceId) {
    runtimeProcessor.checkIsSubFlowInstance(flowInstanceId);
    return runtimeProcessor.terminateProcess(flowInstanceId, true);
  }

  @Override
  public TerminateResult terminateProcess(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    runtimeProcessor.checkIsSubFlowInstance(flowInstanceId);
    return runtimeProcessor.terminateProcess(flowInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public NodeInstanceListResult getHistoryUserTaskList(String flowInstanceId) {
    return runtimeProcessor.getHistoryUserTaskList(flowInstanceId, true);
  }

  @Override
  public NodeInstanceListResult getHistoryUserTaskList(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getHistoryUserTaskList(flowInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public ElementInstanceListResult getHistoryElementList(String flowInstanceId) {
    return runtimeProcessor.getHistoryElementList(flowInstanceId, true);
  }

  @Override
  public ElementInstanceListResult getHistoryElementList(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getHistoryElementList(flowInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public InstanceDataListResult getInstanceData(String flowInstanceId) {
    return runtimeProcessor.getInstanceData(flowInstanceId, true);
  }

  @Override
  public InstanceDataListResult getInstanceData(String flowInstanceId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getInstanceData(flowInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public NodeInstanceResult getNodeInstance(String flowInstanceId, String nodeInstanceId) {
    return runtimeProcessor.getNodeInstance(flowInstanceId, nodeInstanceId, true);
  }

  @Override
  public NodeInstanceResult getNodeInstance(String flowInstanceId, String nodeInstanceId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getNodeInstance(flowInstanceId, nodeInstanceId, effectiveForSubFlowInstance);
  }

  @Override
  public FlowInstanceResult getFlowInstance(String flowInstanceId) {
    return runtimeProcessor.getFlowInstance(flowInstanceId);
  }

  @Override
  public InstanceDataListResult getInstanceData(String flowInstanceId, String instanceDataId) {
    return runtimeProcessor.getInstanceData(flowInstanceId, instanceDataId, true);
  }

  @Override
  public InstanceDataListResult getInstanceData(String flowInstanceId, String instanceDataId, boolean effectiveForSubFlowInstance) {
    return runtimeProcessor.getInstanceData(flowInstanceId, instanceDataId, effectiveForSubFlowInstance);
  }
}
