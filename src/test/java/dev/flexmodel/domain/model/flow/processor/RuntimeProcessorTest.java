package dev.flexmodel.domain.model.flow.processor;

import dev.flexmodel.domain.model.flow.dto.result.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;
import dev.flexmodel.codegen.entity.FlowDeployment;
import dev.flexmodel.domain.model.flow.dto.bo.ElementInstance;
import dev.flexmodel.domain.model.flow.dto.bo.NodeInstance;
import dev.flexmodel.domain.model.flow.dto.param.CommitTaskParam;
import dev.flexmodel.domain.model.flow.dto.param.RollbackTaskParam;
import dev.flexmodel.domain.model.flow.dto.param.StartProcessParam;
import dev.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import dev.flexmodel.domain.model.flow.shared.EntityBuilder;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.shared.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
@Slf4j
public class RuntimeProcessorTest {

  @Inject
  RuntimeProcessor runtimeProcessor;

  @Inject
  FlowDeploymentRepository flowDeploymentRepository;

  private StartProcessResult startProcess() throws Exception {
    // prepare
    FlowDeployment flowDeployment = EntityBuilder.buildSpecialFlowDeployment();
    FlowDeployment _flowDeployment = flowDeploymentRepository.findByDeployId(flowDeployment.getProjectId(), flowDeployment.getFlowDeployId());
    if (_flowDeployment != null) {
      if (!Objects.equals(_flowDeployment.getFlowModel(), flowDeployment.getFlowModel())) {
        flowDeploymentRepository.deleteById(flowDeployment.getProjectId(), _flowDeployment.getId());
        flowDeploymentRepository.insert(flowDeployment);
      }
    } else {
      flowDeploymentRepository.insert(flowDeployment);
    }

    // start process
    StartProcessParam startProcessParam = new StartProcessParam();
    startProcessParam.setProjectId("dev_test");
    startProcessParam.setFlowDeployId(flowDeployment.getFlowDeployId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("orderId", "123");
    variables.put("orderStatus", "1");
    startProcessParam.setVariables(variables);
    // build
    return runtimeProcessor.startProcess(startProcessParam);
  }

  @Test
  public void testStartProcess() throws Exception {
    StartProcessResult startProcessResult = startProcess();
    Assertions.assertEquals(startProcessResult.getErrCode(), ErrorEnum.COMMIT_SUSPEND.getErrNo());
    Assertions.assertEquals("BranchUserTask_0scrl8d", startProcessResult.getActiveTaskInstance().getKey());
  }

  // UserTask -> EndEvent
  @Test
  public void testNormalCommitToEnd() throws Exception {
    StartProcessResult startProcessResult = startProcess();

    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 1);
    commitTaskParam.setVariables(variables);

    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);
    log.info("testCommit.||commitTaskResult={}", commitTaskResult);
    Assertions.assertEquals(commitTaskResult.getErrCode(), ErrorEnum.SUCCESS.getErrNo());
    Assertions.assertEquals("EndEvent_0s4vsxw", commitTaskResult.getActiveTaskInstance().getKey());
  }

  // UserTask -> ExclusiveGateway -> UserTask
  @Test
  public void testNormalCommitToUserTask() throws Exception {
    StartProcessResult startProcessResult = startProcess();

    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 0);
    commitTaskParam.setVariables(variables);

    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);
    log.info("testCommit.||commitTaskResult={}", commitTaskResult);
    Assertions.assertEquals(commitTaskResult.getErrCode(), ErrorEnum.COMMIT_SUSPEND.getErrNo());
    Assertions.assertEquals("UserTask_0uld0u9", commitTaskResult.getActiveTaskInstance().getKey());
  }

  // UserTask -> ExclusiveGateway -> UserTask
  // UserTask ->
  @Test
  public void testRepeatedCommitToUserTask() throws Exception {
    StartProcessResult startProcessResult = startProcess();

    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 0);
    commitTaskParam.setVariables(variables);
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    commitTaskResult = runtimeProcessor.commit(commitTaskParam);
    log.info("testCommit.||commitTaskResult={}", commitTaskResult);

    Assertions.assertEquals(commitTaskResult.getErrCode(), ErrorEnum.COMMIT_SUSPEND.getErrNo());
    Assertions.assertEquals("UserTask_0uld0u9", commitTaskResult.getActiveTaskInstance().getKey());
  }

  // UserTask -> EndEvent -> Commit again
  @Test
  public void testCommitCompletedFlowInstance() throws Exception {
    StartProcessResult startProcessResult = startProcess();

    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 1);
    commitTaskParam.setVariables(variables);
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    commitTaskResult = runtimeProcessor.commit(commitTaskParam);
    log.info("testCommit.||commitTaskResult={}", commitTaskResult);

    Assertions.assertEquals(commitTaskResult.getErrCode(), ErrorEnum.REENTRANT_WARNING.getErrNo());
  }

  @Test
  public void testCommitTerminatedFlowInstance() throws Exception {
    StartProcessResult startProcessResult = startProcess();

    runtimeProcessor.terminateProcess(startProcessResult.getProjectId(), startProcessResult.getFlowInstanceId(), false);

    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 1);
    commitTaskParam.setVariables(variables);
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    Assertions.assertEquals(commitTaskResult.getErrCode(), ErrorEnum.COMMIT_REJECTRD.getErrNo());
  }

  // UserTask <- ExclusiveGateway <- UserTask : Commit old UserTask
  @Test
  public void testRollbackToUserTaskAndCommitOldUserTask() throws Exception {
    // start process
    StartProcessResult startProcessResult = startProcess();
    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 0);
    commitTaskParam.setVariables(variables);

    // UserTask -> ExclusiveGateway -> UserTask
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    // UserTask <- ExclusiveGateway <- UserTask
    RollbackTaskParam rollbackTaskParam = new RollbackTaskParam();
    rollbackTaskParam.setProjectId(startProcess().getProjectId());
    rollbackTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    rollbackTaskParam.setTaskInstanceId(commitTaskResult.getActiveTaskInstance().getNodeInstanceId());
    RollbackTaskResult rollbackTaskResult = runtimeProcessor.rollback(rollbackTaskParam);

    // commit old UserTask
    commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    log.info("testRollbackToUserTaskAndCommitOldUserTask.||commitTaskResult={}", commitTaskResult);
    Assertions.assertEquals(commitTaskResult.getErrCode(), ErrorEnum.COMMIT_FAILED.getErrNo());
    Assertions.assertEquals("BranchUserTask_0scrl8d", commitTaskResult.getActiveTaskInstance().getKey());
  }

  @Test
  public void testRollbackFromMiddleUserTask() throws Exception {
    // start process
    StartProcessResult startProcessResult = startProcess();
    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    String branchUserTaskNodeInstanceId = startProcessResult.getActiveTaskInstance().getNodeInstanceId();
    commitTaskParam.setTaskInstanceId(branchUserTaskNodeInstanceId);
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 0);
    commitTaskParam.setVariables(variables);

    // UserTask -> ExclusiveGateway -> UserTask
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    // StartEvent <- UserTask
    RollbackTaskParam rollbackTaskParam = new RollbackTaskParam();
    rollbackTaskParam.setProjectId(startProcessResult.getProjectId());
    rollbackTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    // Previous UserTask node
    rollbackTaskParam.setTaskInstanceId(branchUserTaskNodeInstanceId);
    RollbackTaskResult rollbackTaskResult = runtimeProcessor.rollback(rollbackTaskParam);

    // Ignore current userTask
    log.info("testRollbackFromMiddleUserTask.||rollbackTaskResult={}", rollbackTaskResult);
    Assertions.assertEquals(rollbackTaskResult.getErrCode(), ErrorEnum.ROLLBACK_SUSPEND.getErrNo());
    Assertions.assertEquals("BranchUserTask_0scrl8d", rollbackTaskResult.getActiveTaskInstance().getKey());
  }


  // UserTask <- ExclusiveGateway <- UserTask
  @Test
  public void testRollbackToUserTask() throws Exception {
    // start process
    StartProcessResult startProcessResult = startProcess();
    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 0);
    commitTaskParam.setVariables(variables);

    // UserTask -> ExclusiveGateway -> UserTask
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    // UserTask <- ExclusiveGateway <- UserTask
    RollbackTaskParam rollbackTaskParam = new RollbackTaskParam();
    rollbackTaskParam.setProjectId(startProcessResult.getProjectId());
    rollbackTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    rollbackTaskParam.setTaskInstanceId(commitTaskResult.getActiveTaskInstance().getNodeInstanceId());
    RollbackTaskResult rollbackTaskResult = runtimeProcessor.rollback(rollbackTaskParam);

    log.info("testRollback.||rollbackTaskResult={}", rollbackTaskResult);
    Assertions.assertEquals(rollbackTaskResult.getErrCode(), ErrorEnum.ROLLBACK_SUSPEND.getErrNo());
    Assertions.assertEquals("BranchUserTask_0scrl8d", rollbackTaskResult.getActiveTaskInstance().getKey());
  }

  // StartEvent <- UserTask
  @Test
  public void testRollbackToStartEvent() throws Exception {
    // start process
    StartProcessResult startProcessResult = startProcess();
    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 0);
    commitTaskParam.setVariables(variables);

    // UserTask -> ExclusiveGateway -> UserTask
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    // UserTask <- ExclusiveGateway <- UserTask
    RollbackTaskParam rollbackTaskParam = new RollbackTaskParam();
    rollbackTaskParam.setProjectId(startProcessResult.getProjectId());
    rollbackTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    rollbackTaskParam.setTaskInstanceId(commitTaskResult.getActiveTaskInstance().getNodeInstanceId());
    RollbackTaskResult rollbackTaskResult = runtimeProcessor.rollback(rollbackTaskParam);

    // StartEvent <- UserTask
    rollbackTaskParam = new RollbackTaskParam();
    rollbackTaskParam.setProjectId(startProcessResult.getProjectId());
    rollbackTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    rollbackTaskParam.setTaskInstanceId(rollbackTaskResult.getActiveTaskInstance().getNodeInstanceId());
    rollbackTaskResult = runtimeProcessor.rollback(rollbackTaskParam);
    log.info("testRollback.||rollbackTaskResult={}", rollbackTaskResult);
    Assertions.assertEquals(rollbackTaskResult.getErrCode(), ErrorEnum.NO_USER_TASK_TO_ROLLBACK.getErrNo());
  }

  // rollback completed process
  @Test
  public void testRollbackFromEndEvent() throws Exception {
    // start process
    StartProcessResult startProcessResult = startProcess();
    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 1);
    commitTaskParam.setVariables(variables);

    // UserTask -> EndEvent
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    // rollback EndEvent
    RollbackTaskParam rollbackTaskParam = new RollbackTaskParam();
    rollbackTaskParam.setProjectId(startProcessResult.getProjectId());
    rollbackTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    rollbackTaskParam.setTaskInstanceId(commitTaskResult.getActiveTaskInstance().getNodeInstanceId());
    RollbackTaskResult rollbackTaskResult = runtimeProcessor.rollback(rollbackTaskParam);

    log.info("testRollback.||rollbackTaskResult={}", rollbackTaskResult);
    Assertions.assertEquals(rollbackTaskResult.getErrCode(), ErrorEnum.ROLLBACK_REJECTRD.getErrNo());
  }

  @Test
  public void testTerminateProcess() throws Exception {
    StartProcessResult startProcessResult = startProcess();
    TerminateResult terminateResult = runtimeProcessor.terminateProcess(startProcessResult.getProjectId(), startProcessResult.getFlowInstanceId(), false);
    log.info("testTerminateProcess.||terminateResult={}", terminateResult);
    Assertions.assertEquals(terminateResult.getErrCode(), ErrorEnum.SUCCESS.getErrNo());
  }

  @Test
  public void testGetHistoryUserTaskList() throws Exception {
    StartProcessResult startProcessResult = startProcess();
    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 0);
    commitTaskParam.setVariables(variables);

    // UserTask -> ExclusiveGateway -> UserTask
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    NodeInstanceListResult nodeInstanceListResult = runtimeProcessor.getHistoryUserTaskList(commitTaskResult.getProjectId(), commitTaskResult.getFlowInstanceId(), false);
    log.info("testGetHistoryUserTaskList.||nodeInstanceListResult={}", nodeInstanceListResult);
    StringBuilder sb = new StringBuilder();
    for (NodeInstance elementInstanceResult : nodeInstanceListResult.getNodeInstanceList()) {
      sb.append("[");
      sb.append(elementInstanceResult.getKey());
      sb.append(" ");
      sb.append(elementInstanceResult.getStatus());
      sb.append("]->");
    }
    log.info("testGetHistoryUserTaskList.||snapshot={}", sb.toString());

    Assertions.assertEquals(2, nodeInstanceListResult.getNodeInstanceList().size());
    Assertions.assertEquals("UserTask_0uld0u9", nodeInstanceListResult.getNodeInstanceList().getFirst().getKey());
  }


  @Test
  public void testGetFailedHistoryElementList() throws Exception {
    StartProcessResult startProcessResult = startProcess();
    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 0);
    variables.put("orderId", "notExistOrderId");
    commitTaskParam.setVariables(variables);

    // UserTask -> ExclusiveGateway : Failed
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);
    log.info("testGetFailedHistoryElementList.||commitTaskResult={}", commitTaskResult);
    Assertions.assertEquals(commitTaskResult.getErrCode(), ErrorEnum.GET_OUTGOING_FAILED.getErrNo());

    ElementInstanceListResult elementInstanceListResult = runtimeProcessor.getHistoryElementList(commitTaskResult.getProjectId(), commitTaskResult.getFlowInstanceId(), false);
    log.info("testGetHistoryElementList.||elementInstanceListResult={}", elementInstanceListResult);
    StringBuilder sb = new StringBuilder();
    for (ElementInstance elementInstanceResult : elementInstanceListResult.getElementInstanceList()) {
      sb.append("[");
      sb.append(elementInstanceResult.getKey());
      sb.append(" ");
      sb.append(elementInstanceResult.getStatus());
      sb.append("]->");
    }
    log.info("testGetHistoryElementList.||snapshot={}", sb.toString());

    Assertions.assertEquals(5, elementInstanceListResult.getElementInstanceList().size());
    Assertions.assertEquals("ExclusiveGateway_0yq2l0s", elementInstanceListResult.getElementInstanceList().get(4).getKey());
  }

  @Test
  public void testGetCompletedHistoryElementList() throws Exception {
    StartProcessResult startProcessResult = startProcess();
    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 1);
    commitTaskParam.setVariables(variables);

    // UserTask -> EndEvent
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    ElementInstanceListResult elementInstanceListResult = runtimeProcessor.getHistoryElementList(commitTaskResult.getProjectId(), commitTaskResult.getFlowInstanceId(), false);
    log.info("testGetHistoryElementList.||elementInstanceListResult={}", elementInstanceListResult);
    StringBuilder sb = new StringBuilder();
    for (ElementInstance elementInstanceResult : elementInstanceListResult.getElementInstanceList()) {
      sb.append("[");
      sb.append(elementInstanceResult.getKey());
      sb.append(" ");
      sb.append(elementInstanceResult.getStatus());
      sb.append("]->");
    }
    log.info("testGetHistoryElementList.||snapshot={}", sb.toString());

    Assertions.assertEquals(5, elementInstanceListResult.getElementInstanceList().size());
    Assertions.assertEquals("EndEvent_0s4vsxw", elementInstanceListResult.getElementInstanceList().get(4).getKey());
  }


  @Test
  public void testGetInstanceData() throws Exception {
    StartProcessResult startProcessResult = startProcess();
    String flowInstanceId = startProcessResult.getFlowInstanceId();
    InstanceDataListResult instanceDataList = runtimeProcessor.getInstanceData(startProcessResult.getProjectId(), flowInstanceId, false);
    log.info("testGetInstanceData 1.||instanceDataList={}", instanceDataList);

    CommitTaskParam commitTaskParam = new CommitTaskParam();
    commitTaskParam.setProjectId(startProcessResult.getProjectId());
    commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables = new HashMap<>();
    variables.put("danxuankuang_ytgyk", 0);
    variables.put("commitTime", 1);
    commitTaskParam.setVariables(variables);

    // UserTask -> ExclusiveGateway -> UserTask
    CommitTaskResult commitTaskResult = runtimeProcessor.commit(commitTaskParam);

    // UserTask -> UserTask
    CommitTaskParam commitTaskParam1 = new CommitTaskParam();
    commitTaskParam1.setProjectId(startProcessResult.getProjectId());
    commitTaskParam1.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    commitTaskParam1.setTaskInstanceId(commitTaskResult.getActiveTaskInstance().getNodeInstanceId());
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("orderStatus", "2");
    variables1.put("commitTime", 2);
    commitTaskParam1.setVariables(variables1);
    CommitTaskResult commitTaskResult1 = runtimeProcessor.commit(commitTaskParam1);

    instanceDataList = runtimeProcessor.getInstanceData(commitTaskResult.getProjectId(), flowInstanceId, false);
    log.info("testGetInstanceData 2.||instanceDataList={}", instanceDataList);

    // UserTask <- UserTask
    RollbackTaskParam rollbackTaskParam = new RollbackTaskParam();
    rollbackTaskParam.setProjectId(startProcessResult.getProjectId());
    rollbackTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    rollbackTaskParam.setTaskInstanceId(commitTaskResult1.getActiveTaskInstance().getNodeInstanceId());
    RollbackTaskResult rollbackTaskResult = runtimeProcessor.rollback(rollbackTaskParam);
    log.info("rollbackTaskResult 3.||rollbackTaskResult.variables={}", rollbackTaskResult.getVariables());
    // UserTask <- ExclusiveGateway <- UserTask
    RollbackTaskParam rollbackTaskParam1 = new RollbackTaskParam();
    rollbackTaskParam1.setProjectId(startProcessResult.getProjectId());
    rollbackTaskParam1.setFlowInstanceId(startProcessResult.getFlowInstanceId());
    rollbackTaskParam1.setTaskInstanceId(rollbackTaskResult.getActiveTaskInstance().getNodeInstanceId());
    RollbackTaskResult rollbackTaskResult1 = runtimeProcessor.rollback(rollbackTaskParam1);
    log.info("rollbackTaskResult 4.||rollbackTaskResult.variables={}", rollbackTaskResult1.getVariables());

    instanceDataList = runtimeProcessor.getInstanceData(rollbackTaskParam1.getProjectId(), flowInstanceId, false);
    log.info("testGetInstanceData 5.||instanceDataList={}", instanceDataList);
    String initData = JsonUtils.getInstance().stringify(startProcessResult.getVariables());
    String rollbackData = JsonUtils.getInstance().stringify(rollbackTaskResult1.getVariables());
    Assertions.assertEquals(initData, rollbackData);
  }

  @Test
  public void testGetNodeInstance() throws Exception {
    StartProcessResult startProcessResult = startProcess();
    String flowInstanceId = startProcessResult.getFlowInstanceId();
    NodeInstanceResult nodeInstanceResult = runtimeProcessor.getNodeInstance(startProcessResult.getProjectId(), flowInstanceId, startProcessResult.getActiveTaskInstance().getNodeInstanceId(), false);
    log.info("testGetNodeInstance.||nodeInstanceResult={}", nodeInstanceResult);

    Assertions.assertEquals(nodeInstanceResult.getNodeInstance().getNodeInstanceId(), startProcessResult.getActiveTaskInstance().getNodeInstanceId());
  }
}
