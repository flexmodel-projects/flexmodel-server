package tech.wetech.flexmodel.domain.model.flow.dto.result;

import com.google.common.base.MoreObjects;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.FlowInstanceBO;

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
    return MoreObjects.toStringHelper(this)
      .add("errCode", getErrCode())
      .add("errMsg", getErrMsg())
      .add("flowInstanceBO", flowInstance)
      .toString();
  }
}
