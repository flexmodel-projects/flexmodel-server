package tech.wetech.flexmodel.domain.model.flow.dto.result;

import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;

public class TerminateResult extends RuntimeResult {

  public TerminateResult(ErrorEnum errorEnum) {
    super(errorEnum);
  }

}
