package tech.wetech.flexmodel.domain.model.flow.dto.result;

import com.google.common.base.MoreObjects;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance;

public class NodeInstanceResult extends CommonResult {
  private NodeInstance nodeInstance;

  public NodeInstance getNodeInstance() {
    return nodeInstance;
  }

  public void setNodeInstance(NodeInstance nodeInstance) {
    this.nodeInstance = nodeInstance;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("errCode", getErrCode())
      .add("errMsg", getErrMsg())
      .add("nodeInstance", nodeInstance)
      .toString();
  }
}
