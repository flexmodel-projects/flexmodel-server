package dev.flexmodel.domain.model.flow.validator;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
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
public class ElementValidatorFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElementValidatorFactory.class);

  @Inject
  StartEventValidator startEventValidator;

  @Inject
  EndEventValidator endEventValidator;

  @Inject
  SequenceFlowValidator sequenceFlowValidator;

  @Inject
  UserTaskValidator userTaskValidator;

  @Inject
  ServiceTaskValidator serviceTaskValidator;

  @Inject
  ExclusiveGatewayValidator exclusiveGatewayValidator;

  @Inject
  CallActivityValidator callActivityValidator;

  @Inject
  PluginManager pluginManager;

  private final Map<Integer, ElementValidator> validatorMap = new HashMap<>(16);

  /**
   * 将原生校验器与插件扩展校验器汇总
   * 插件扩展校验器可以通过设置与原生校验器相同的elementType值进行覆盖
   */
  @PostConstruct
  public void init() {
    validatorMap.put(FlowElementType.SEQUENCE_FLOW, sequenceFlowValidator);
    validatorMap.put(FlowElementType.START_EVENT, startEventValidator);
    validatorMap.put(FlowElementType.END_EVENT, endEventValidator);
    validatorMap.put(FlowElementType.USER_TASK, userTaskValidator);
    validatorMap.put(FlowElementType.SERVICE_TASK, serviceTaskValidator);
    validatorMap.put(FlowElementType.EXCLUSIVE_GATEWAY, exclusiveGatewayValidator);
    validatorMap.put(FlowElementType.CALL_ACTIVITY, callActivityValidator);
    List<ElementPlugin> elementPlugins = pluginManager.getPluginsFor(ElementPlugin.class);
    elementPlugins.forEach(elementPlugin -> validatorMap.put(elementPlugin.getFlowElementType(), elementPlugin.getElementValidator()));

  }

  public ElementValidator getElementValidator(FlowElement flowElement) throws ProcessException {
    int elementType = flowElement.getType();
    ElementValidator elementValidator = getElementValidator(elementType);

    if (elementValidator == null) {
      LOGGER.warn("getElementValidator failed: unsupported elementType.||elementType={}", elementType);
      throw new ProcessException(ErrorEnum.UNSUPPORTED_ELEMENT_TYPE,
        MessageFormat.format(Constants.NODE_INFO_FORMAT, flowElement.getKey(),
          FlowModelUtil.getElementName(flowElement), elementType));
    }
    return elementValidator;
  }

  private ElementValidator getElementValidator(int elementType) {
    return validatorMap.get(elementType);
  }
}
