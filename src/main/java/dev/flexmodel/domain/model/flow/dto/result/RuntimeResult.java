package dev.flexmodel.domain.model.flow.dto.result;

import dev.flexmodel.domain.model.flow.dto.bo.NodeInstance;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuntimeResult extends CommonResult {
  private String flowInstanceId;
  private int status;
  private List<NodeExecuteResult> nodeExecuteResults;

  private Map<String, Object> extendProperties;

  public RuntimeResult() {
    super();
  }

  public RuntimeResult(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  public String getFlowInstanceId() {
    return flowInstanceId;
  }

  public void setFlowInstanceId(String flowInstanceId) {
    this.flowInstanceId = flowInstanceId;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  // 兼容旧版本
  public NodeInstance getActiveTaskInstance() {
    if (nodeExecuteResults == null || nodeExecuteResults.isEmpty()) {
      return null;
    }
    return nodeExecuteResults.get(0).activeTaskInstance;
  }

  // 兼容旧版本
  public void setActiveTaskInstance(NodeInstance activeTaskInstance) {
    if (nodeExecuteResults == null) {
      this.nodeExecuteResults = new ArrayList<>();
    }
    if (nodeExecuteResults.isEmpty()) {
      this.nodeExecuteResults.add(new NodeExecuteResult());
    }
    this.nodeExecuteResults.get(0).activeTaskInstance = activeTaskInstance;
  }

  // 兼容旧版本
  public Map<String, Object> getVariables() {
    if (nodeExecuteResults == null || nodeExecuteResults.isEmpty()) {
      return null;
    }
    return nodeExecuteResults.get(0).variables;
  }

  // 兼容旧版本
  public void setVariables(Map<String, Object> variables) {
    if (nodeExecuteResults == null) {
      this.nodeExecuteResults = new ArrayList<>();
    }
    if (nodeExecuteResults.isEmpty()) {
      this.nodeExecuteResults.add(new NodeExecuteResult());
    }
    this.nodeExecuteResults.get(0).variables = variables;
  }

  public List<NodeExecuteResult> getNodeExecuteResults() {
    return nodeExecuteResults;
  }

  public void setNodeExecuteResults(List<NodeExecuteResult> nodeExecuteResults) {
    this.nodeExecuteResults = nodeExecuteResults;
  }

  public Map<String, Object> getExtendProperties() {
    return extendProperties;
  }

  public void setExtendProperties(Map<String, Object> extendProperties) {
    this.extendProperties = extendProperties;
  }

  @Override
  public String toString() {
    return "RuntimeResult{" +
           "flowInstanceId='" + flowInstanceId + '\'' +
           ", status=" + status +
           ", nodeExecuteResults=" + nodeExecuteResults +
           ", extendProperties=" + extendProperties +
           '}';
  }

  public static class NodeExecuteResult extends CommonResult {
    private NodeInstance activeTaskInstance;
    private Map<String, Object> variables;

    public NodeInstance getActiveTaskInstance() {
      return activeTaskInstance;
    }

    public void setActiveTaskInstance(NodeInstance activeTaskInstance) {
      this.activeTaskInstance = activeTaskInstance;
    }

    public Map<String, Object> getVariables() {
      return variables;
    }

    public void setVariables(Map<String, Object> variables) {
      this.variables = variables;
    }

    @Override
    public String toString() {
      return "NodeExecuteResult{" +
             "activeTaskInstance=" + activeTaskInstance +
             ", variables=" + variables +
             '}';
    }
  }
}
