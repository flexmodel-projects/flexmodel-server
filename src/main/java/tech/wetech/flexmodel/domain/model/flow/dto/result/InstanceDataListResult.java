package tech.wetech.flexmodel.domain.model.flow.dto.result;

import tech.wetech.flexmodel.domain.model.flow.dto.model.InstanceData;
import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;

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
    return "InstanceDataListResult{" +
           "variables=" + variables +
           '}';
  }
}
