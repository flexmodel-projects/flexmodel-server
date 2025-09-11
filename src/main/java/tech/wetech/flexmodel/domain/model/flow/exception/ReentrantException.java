package tech.wetech.flexmodel.domain.model.flow.exception;

import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;

public class ReentrantException extends ProcessException {

  public ReentrantException(int errNo, String errMsg) {
    super(errNo, errMsg);
  }

  public ReentrantException(ErrorEnum errorEnum) {
    super(errorEnum);
  }
}
