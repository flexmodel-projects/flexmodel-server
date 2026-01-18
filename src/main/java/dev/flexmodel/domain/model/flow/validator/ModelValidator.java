package dev.flexmodel.domain.model.flow.validator;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.domain.model.flow.dto.model.FlowModel;
import dev.flexmodel.domain.model.flow.dto.param.CommonParam;
import dev.flexmodel.domain.model.flow.exception.DefinitionException;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.domain.model.flow.shared.util.FlowModelUtil;
import dev.flexmodel.shared.utils.CollectionUtils;
import dev.flexmodel.shared.utils.StringUtils;

@Singleton
public class ModelValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelValidator.class);

  @Inject
  FlowModelValidator flowModelValidator;

  public void validate(String flowModelStr) throws DefinitionException, ProcessException {
    this.validate(flowModelStr, null);
  }

  public void validate(String flowModelStr, CommonParam commonParam) throws DefinitionException, ProcessException {
    if (StringUtils.isBlank(flowModelStr)) {
      LOGGER.warn("message={}", ErrorEnum.MODEL_EMPTY.getErrMsg());
      throw new DefinitionException(ErrorEnum.MODEL_EMPTY);
    }

    FlowModel flowModel = FlowModelUtil.parseModelFromString(flowModelStr);
    if (flowModel == null || CollectionUtils.isEmpty(flowModel.getFlowElementList())) {
      LOGGER.warn("message={}||flowModelStr={}", ErrorEnum.MODEL_EMPTY.getErrMsg(), flowModelStr);
      throw new DefinitionException(ErrorEnum.MODEL_EMPTY);
    }
    flowModelValidator.validate(flowModel, commonParam);
  }
}
