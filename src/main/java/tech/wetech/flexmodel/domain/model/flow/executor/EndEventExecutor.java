package tech.wetech.flexmodel.domain.model.flow.executor;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.domain.model.flow.bo.NodeInstanceBO;
import tech.wetech.flexmodel.domain.model.flow.common.Constants;
import tech.wetech.flexmodel.domain.model.flow.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.common.NodeInstanceStatus;
import tech.wetech.flexmodel.domain.model.flow.common.RuntimeContext;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;
import tech.wetech.flexmodel.domain.model.flow.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.util.FlowModelUtil;

import java.text.MessageFormat;

@Singleton
public class EndEventExecutor extends ElementExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(EndEventExecutor.class);

  @Override
  protected void postExecute(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    currentNodeInstance.setInstanceDataId(runtimeContext.getInstanceDataId());
    currentNodeInstance.setStatus(NodeInstanceStatus.COMPLETED);
    runtimeContext.getNodeInstanceList().add(currentNodeInstance);
  }

  @Override
  protected void doRollback(RuntimeContext runtimeContext) throws ProcessException {
    // when subFlowInstance, the EndEvent rollback is allowed
    if (isSubFlowInstance(runtimeContext)) {
      return;
    }
    FlowElement flowElement = runtimeContext.getCurrentNodeModel();
    String nodeName = FlowModelUtil.getElementName(flowElement);
    LOGGER.warn("doRollback: unsupported element type as EndEvent.||flowInstanceId={}||nodeKey={}||nodeName={}||nodeType={}",
      runtimeContext.getFlowInstanceId(), flowElement.getKey(), nodeName, flowElement.getType());
    throw new ProcessException(ErrorEnum.UNSUPPORTED_ELEMENT_TYPE,
      MessageFormat.format(Constants.NODE_INFO_FORMAT, flowElement.getKey(), nodeName, flowElement.getType()));
  }

  @Override
  protected void postRollback(RuntimeContext runtimeContext) throws ProcessException {
    // when subFlowInstance, the EndEvent rollback is allowed
    if (isSubFlowInstance(runtimeContext)) {
      super.postRollback(runtimeContext);
      return;
    }
    //do nothing
  }

  @Override
  protected RuntimeExecutor getExecuteExecutor(RuntimeContext runtimeContext) throws ProcessException {
    LOGGER.info("getExecuteExecutor: no executor after EndEvent.");
    return null;
  }
}
