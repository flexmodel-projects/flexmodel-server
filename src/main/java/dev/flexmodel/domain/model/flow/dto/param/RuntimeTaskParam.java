package dev.flexmodel.domain.model.flow.dto.param;

import dev.flexmodel.domain.model.flow.shared.common.RuntimeContext;

import java.util.HashMap;
import java.util.Map;

public class RuntimeTaskParam {
  private String projectId;
  private String flowInstanceId;
  private String taskInstanceId;
  // For internal transmission runtimeContext
  private RuntimeContext runtimeContext;
  private Map<String, Object> extendProperties = new HashMap<>(16);

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getFlowInstanceId() {
    return flowInstanceId;
  }

  public void setFlowInstanceId(String flowInstanceId) {
    this.flowInstanceId = flowInstanceId;
  }

  public String getTaskInstanceId() {
    return taskInstanceId;
  }

  public void setTaskInstanceId(String taskInstanceId) {
    this.taskInstanceId = taskInstanceId;
  }

  public RuntimeContext getRuntimeContext() {
    return runtimeContext;
  }

  public void setRuntimeContext(RuntimeContext runtimeContext) {
    this.runtimeContext = runtimeContext;
  }

  public Map<String, Object> getExtendProperties() {
    return extendProperties;
  }

  public void setExtendProperties(Map<String, Object> extendProperties) {
    this.extendProperties = extendProperties;
  }

  @Override
  public String toString() {
    return "RuntimeTaskParam{" +
           "flowInstanceId='" + flowInstanceId + '\'' +
           ", taskInstanceId='" + taskInstanceId + '\'' +
           ", runtimeContext=" + runtimeContext +
           ", extendProperties=" + extendProperties +
           '}';
  }
}
