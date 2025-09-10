package tech.wetech.flexmodel.domain.model.flow.exception;

import tech.wetech.flexmodel.domain.model.flow.common.ErrorEnum;

public class DefinitionException extends TurboException {

  public DefinitionException(int errNo, String errMsg) {
    super(errNo, errMsg);
  }

  public DefinitionException(ErrorEnum errorEnum) {
    super(errorEnum);
  }
}
