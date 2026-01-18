package dev.flexmodel.domain.model.flow.dto.result;

public class StartProcessResult extends RuntimeResult {
  private String flowDeployId;
  private String flowModuleId;

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

  @Override
  public String toString() {
    return "StartProcessResult{" +
           "flowDeployId='" + flowDeployId + '\'' +
           ", flowModuleId='" + flowModuleId + '\'' +
           '}';
  }
}
