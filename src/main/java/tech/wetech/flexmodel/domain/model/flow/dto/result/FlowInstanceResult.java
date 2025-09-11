package tech.wetech.flexmodel.domain.model.flow.dto.result;

import com.google.common.base.MoreObjects;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.FlowInstanceBO;

public class FlowInstanceResult extends CommonResult {

  private FlowInstanceBO flowInstanceBO;

  public FlowInstanceBO getFlowInstanceBO() {
    return flowInstanceBO;
  }

  public void setFlowInstanceBO(FlowInstanceBO flowInstanceBO) {
    this.flowInstanceBO = flowInstanceBO;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("errCode", getErrCode())
      .add("errMsg", getErrMsg())
      .add("flowInstanceBO", flowInstanceBO)
      .toString();
  }
}
