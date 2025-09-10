package tech.wetech.flexmodel.domain.model.flow.result;

import com.google.common.base.MoreObjects;
import tech.wetech.flexmodel.domain.model.flow.common.ErrorEnum;

public class TerminateResult extends RuntimeResult {

  public TerminateResult(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("errCode", getErrCode())
      .add("errMsg", getErrMsg())
      .toString();
  }
}
