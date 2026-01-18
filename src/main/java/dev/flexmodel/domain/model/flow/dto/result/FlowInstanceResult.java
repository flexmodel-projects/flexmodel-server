package dev.flexmodel.domain.model.flow.dto.result;

import dev.flexmodel.domain.model.flow.dto.bo.FlowInstanceBO;

public class FlowInstanceResult extends CommonResult {

  private FlowInstanceBO flowInstance;

  public FlowInstanceBO getFlowInstance() {
    return flowInstance;
  }

  public void setFlowInstance(FlowInstanceBO flowInstanceBO) {
    this.flowInstance = flowInstanceBO;
  }

  @Override
  public String toString() {
    return "FlowInstanceResult{" +
           "flowInstance=" + flowInstance +
           '}';
  }
}
