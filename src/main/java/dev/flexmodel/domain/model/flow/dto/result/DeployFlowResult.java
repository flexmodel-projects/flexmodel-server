package dev.flexmodel.domain.model.flow.dto.result;

public class DeployFlowResult extends CommonResult {
  private String flowModuleId;
  private String flowDeployId;

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
    return "DeployFlowResult{" +
           "flowModuleId='" + flowModuleId + '\'' +
           ", flowDeployId='" + flowDeployId + '\'' +
           '}';
  }
}
