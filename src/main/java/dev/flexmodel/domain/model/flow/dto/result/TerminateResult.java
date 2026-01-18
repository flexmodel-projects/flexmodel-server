package dev.flexmodel.domain.model.flow.dto.result;

import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;

public class TerminateResult extends RuntimeResult {

  public TerminateResult(ErrorEnum errorEnum) {
    super(errorEnum);
  }

}
