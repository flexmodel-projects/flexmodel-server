package dev.flexmodel.domain.model.flow.executor;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.executor.callactivity.SyncSingleCallActivityExecutor;
import dev.flexmodel.domain.model.flow.plugin.ElementPlugin;
import dev.flexmodel.domain.model.flow.plugin.manager.PluginManager;
import dev.flexmodel.domain.model.flow.shared.common.Constants;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.domain.model.flow.shared.common.FlowElementType;
import dev.flexmodel.domain.model.flow.shared.util.FlowModelUtil;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ExecutorFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorFactory.class);

  @Inject
  StartEventExecutor startEventExecutor;

  @Inject
  EndEventExecutor endEventExecutor;

  @Inject
  SequenceFlowExecutor sequenceFlowExecutor;

  @Inject
  UserTaskExecutor userTaskExecutor;

  @Inject
  ServiceTaskExecutor serviceTaskExecutor;

  @Inject
  ExclusiveGatewayExecutor exclusiveGatewayExecutor;

  @Inject
  SyncSingleCallActivityExecutor syncSingleCallActivityExecutor;

  @Inject
  PluginManager pluginManager;

  private final Map<Integer, ElementExecutor> executorMap = new HashMap<>(16);

  /**
   * 将原生执行器与插件扩展执行器汇总
   * 插件扩展执行器可以通过设置与原生执行器相同的elementType值进行覆盖
   */
  @PostConstruct
  public void init() {
    executorMap.put(FlowElementType.SEQUENCE_FLOW, sequenceFlowExecutor);
    executorMap.put(FlowElementType.START_EVENT, startEventExecutor);
    executorMap.put(FlowElementType.END_EVENT, endEventExecutor);
    executorMap.put(FlowElementType.USER_TASK, userTaskExecutor);
    executorMap.put(FlowElementType.SERVICE_TASK, serviceTaskExecutor);
    executorMap.put(FlowElementType.EXCLUSIVE_GATEWAY, exclusiveGatewayExecutor);
    List<ElementPlugin> elementPlugins = pluginManager.getPluginsFor(ElementPlugin.class);
    elementPlugins.forEach(elementPlugin -> executorMap.put(elementPlugin.getFlowElementType(), elementPlugin.getElementExecutor()));
  }


  public ElementExecutor getElementExecutor(FlowElement flowElement) throws ProcessException {
    ElementExecutor elementExecutor = getElementExecutorInternal(flowElement);

    if (elementExecutor == null) {
      LOGGER.warn("getElementExecutor failed: unsupported elementType.|elementType={}", flowElement.getType());
      throw new ProcessException(ErrorEnum.UNSUPPORTED_ELEMENT_TYPE,
        MessageFormat.format(Constants.NODE_INFO_FORMAT, flowElement.getKey(),
          FlowModelUtil.getElementName(flowElement), flowElement.getType()));
    }

    return elementExecutor;
  }

  private ElementExecutor getElementExecutorInternal(FlowElement flowElement) {
    int elementType = flowElement.getType();
    if (elementType == FlowElementType.CALL_ACTIVITY) {
      return getCallActivityExecutor(flowElement);
    }
    return executorMap.get(elementType);
  }

  private ElementExecutor getCallActivityExecutor(FlowElement flowElement) {
    int elementType = flowElement.getType();
    if (FlowElementType.CALL_ACTIVITY != elementType) {
      return null;
    }
    Map<String, Object> properties = flowElement.getProperties();
    String callActivityExecuteType = Constants.CALL_ACTIVITY_EXECUTE_TYPE.SYNC;
    if (properties.containsKey(Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_EXECUTE_TYPE)) {
      callActivityExecuteType = properties.get(Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_EXECUTE_TYPE).toString();
    }
    String callActivityInstanceType = Constants.CALL_ACTIVITY_INSTANCE_TYPE.SINGLE;
    if (properties.containsKey(Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_INSTANCE_TYPE)) {
      callActivityInstanceType = properties.get(Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_INSTANCE_TYPE).toString();
    }

    if (callActivityExecuteType.equals(Constants.CALL_ACTIVITY_EXECUTE_TYPE.SYNC)
        && callActivityInstanceType.equals(Constants.CALL_ACTIVITY_INSTANCE_TYPE.SINGLE)) {
      return syncSingleCallActivityExecutor;
    } else {
      return null;
    }
  }
}
