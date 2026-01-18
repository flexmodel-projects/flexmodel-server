package dev.flexmodel.domain.model.flow.exception;

import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;

public class DefinitionException extends TurboException {

  public DefinitionException(int errNo, String errMsg) {
    super(errNo, errMsg);
  }

  public DefinitionException(ErrorEnum errorEnum) {
    super(errorEnum);
  }
}
