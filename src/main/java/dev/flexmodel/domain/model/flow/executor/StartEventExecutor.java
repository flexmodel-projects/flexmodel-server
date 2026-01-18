package dev.flexmodel.domain.model.flow.executor;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.domain.model.flow.shared.common.NodeInstanceStatus;
import dev.flexmodel.domain.model.flow.shared.common.RuntimeContext;

import java.util.Collections;

@Singleton
public class StartEventExecutor extends ElementExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(StartEventExecutor.class);

  @Override
  protected void postExecute(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    currentNodeInstance.setInstanceDataId(runtimeContext.getInstanceDataId());
    currentNodeInstance.setStatus(NodeInstanceStatus.COMPLETED);
    runtimeContext.getNodeInstanceList().add(currentNodeInstance);
  }

  @Override
  protected void preRollback(RuntimeContext runtimeContext) throws ProcessException {
    // when subFlowInstance, the StartEvent rollback is allowed
    if (isSubFlowInstance(runtimeContext)) {
      super.preRollback(runtimeContext);
      return;
    }
    runtimeContext.setCurrentNodeInstance(runtimeContext.getSuspendNodeInstance());
    runtimeContext.setNodeInstanceList(Collections.emptyList());

    LOGGER.warn("postRollback: reset runtimeContext.||flowInstanceId={}||nodeKey={}||nodeType={}",
      runtimeContext.getFlowInstanceId(), runtimeContext.getCurrentNodeModel().getKey(), runtimeContext.getCurrentNodeModel().getType());
    throw new ProcessException(ErrorEnum.NO_USER_TASK_TO_ROLLBACK, "It's a startEvent.");
  }

  @Override
  protected void postRollback(RuntimeContext runtimeContext) throws ProcessException {
    // when subFlowInstance, the StartEvent rollback is allowed
    if (isSubFlowInstance(runtimeContext)) {
      super.postRollback(runtimeContext);
    }
  }
}
