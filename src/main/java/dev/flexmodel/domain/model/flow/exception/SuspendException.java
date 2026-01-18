package dev.flexmodel.domain.model.flow.exception;


import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;

public class SuspendException extends ProcessException {

  public SuspendException(int errNo, String errMsg) {
    super(errNo, errMsg);
  }

  public SuspendException(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  public SuspendException(ErrorEnum errorEnum, String detailMsg) {
    super(errorEnum, detailMsg);
  }
}
