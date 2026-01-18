package dev.flexmodel.domain.model.flow.dto.param;

public class GetFlowModuleParam {
  private String projectId;
  private String flowModuleId;
  private String flowDeployId;

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

  @Override
  public String toString() {
    return "GetFlowModuleParam{" +
           "flowModuleId='" + flowModuleId + '\'' +
           ", flowDeployId='" + flowDeployId + '\'' +
           '}';
  }
}
