package tech.wetech.flexmodel.domain.model.flow.result;

import com.google.common.base.MoreObjects;
import tech.wetech.flexmodel.domain.model.flow.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.model.InstanceData;

import java.util.List;

public class InstanceDataListResult extends CommonResult {
  private List<InstanceData> variables;

  public InstanceDataListResult(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  public List<InstanceData> getVariables() {
    return variables;
  }

  public void setVariables(List<InstanceData> variables) {
    this.variables = variables;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("errCode", getErrCode())
      .add("errMsg", getErrMsg())
      .add("variables", variables)
      .toString();
  }
}
