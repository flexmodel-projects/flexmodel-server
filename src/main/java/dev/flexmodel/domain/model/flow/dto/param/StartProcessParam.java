package dev.flexmodel.domain.model.flow.dto.param;

import dev.flexmodel.domain.model.flow.shared.common.RuntimeContext;

import java.util.Map;

public class StartProcessParam {
  // For internal transmission runtimeContext
  private RuntimeContext runtimeContext;
  private String projectId;
  private String flowModuleId;
  private String flowDeployId;
  private Map<String, Object> variables;

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getFlowModuleId() {
    return flowModuleId;
  }

  public void setFlowModuleId(String flowModuleId) {
    this.flowModuleId = flowModuleId;
  }

  public String getFlowDeployId() {
    return flowDeployId;
  }

  public void setFlowDeployId(String flowDeployId) {
    this.flowDeployId = flowDeployId;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  public void setRuntimeContext(RuntimeContext runtimeContext) {
    this.runtimeContext = runtimeContext;
  }

  public RuntimeContext getRuntimeContext() {
    return runtimeContext;
  }

  @Override
  public String toString() {
    return "StartProcessParam{" +
           "runtimeContext=" + runtimeContext +
           ", flowModuleId='" + flowModuleId + '\'' +
           ", flowDeployId='" + flowDeployId + '\'' +
           ", variables=" + variables +
           '}';
  }
}
