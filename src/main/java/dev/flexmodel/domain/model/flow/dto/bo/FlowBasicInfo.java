package dev.flexmodel.domain.model.flow.dto.bo;

public class FlowBasicInfo {
  private String flowDeployId;
  private String flowModuleId;
  private String projectId;
  private String caller;

  public String getFlowDeployId() {
    return flowDeployId;
  }

  public void setFlowDeployId(String flowDeployId) {
    this.flowDeployId = flowDeployId;
  }

  public String getFlowModuleId() {
    return flowModuleId;
  }

  public void setFlowModuleId(String flowModuleId) {
    this.flowModuleId = flowModuleId;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getCaller() {
    return caller;
  }

  public void setCaller(String caller) {
    this.caller = caller;
  }

  @Override
  public String toString() {
    return "FlowBasicInfo{" +
           "flowDeployId='" + flowDeployId + '\'' +
           ", flowModuleId='" + flowModuleId + '\'' +
           ", tenant='" + projectId + '\'' +
           ", caller='" + caller + '\'' +
           '}';
  }
}
