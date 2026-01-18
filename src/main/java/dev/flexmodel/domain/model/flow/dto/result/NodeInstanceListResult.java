package dev.flexmodel.domain.model.flow.dto.result;

import dev.flexmodel.domain.model.flow.dto.bo.NodeInstance;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;

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
    return "NodeInstanceListResult{" +
           "nodeInstanceList=" + nodeInstanceList +
           '}';
  }
}
