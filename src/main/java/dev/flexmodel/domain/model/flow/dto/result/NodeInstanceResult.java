package dev.flexmodel.domain.model.flow.dto.result;

import dev.flexmodel.domain.model.flow.dto.bo.NodeInstance;

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
    return "NodeInstanceResult{" +
           "nodeInstance=" + nodeInstance +
           "} " + super.toString();
  }
}
