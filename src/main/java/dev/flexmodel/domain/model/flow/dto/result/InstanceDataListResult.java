package dev.flexmodel.domain.model.flow.dto.result;

import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;

import java.util.Map;

public class InstanceDataListResult extends CommonResult {
  private Map<String, Object> variables;

  public InstanceDataListResult(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  @Override
  public String toString() {
    return "InstanceDataListResult{" +
           "variables=" + variables +
           '}';
  }
}
