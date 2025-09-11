package tech.wetech.flexmodel.domain.model.flow.dto.result;

import com.google.common.base.MoreObjects;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstance;
import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;

import java.util.List;

public class NodeInstanceListResult extends CommonResult {
  private List<NodeInstance> nodeInstanceList;

  public NodeInstanceListResult(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  public List<NodeInstance> getNodeInstanceList() {
    return nodeInstanceList;
  }

  public void setNodeInstanceList(List<NodeInstance> nodeInstanceList) {
    this.nodeInstanceList = nodeInstanceList;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("errCode", getErrCode())
      .add("errMsg", getErrMsg())
      .add("nodeInstanceList", nodeInstanceList)
      .toString();
  }
}
