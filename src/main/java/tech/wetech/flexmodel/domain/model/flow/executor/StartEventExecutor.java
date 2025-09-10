package tech.wetech.flexmodel.domain.model.flow.executor;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.domain.model.flow.bo.NodeInstanceBO;
import tech.wetech.flexmodel.domain.model.flow.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.common.NodeInstanceStatus;
import tech.wetech.flexmodel.domain.model.flow.common.RuntimeContext;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;

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
