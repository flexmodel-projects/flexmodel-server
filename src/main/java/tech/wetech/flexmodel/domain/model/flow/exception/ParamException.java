package tech.wetech.flexmodel.domain.model.flow.exception;

import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;

public class ParamException extends TurboException {

  public ParamException(int errNo, String errMsg) {
    super(errNo, errMsg);
  }

  public ParamException(ErrorEnum errorEnum) {
    super(errorEnum);
  }
}

