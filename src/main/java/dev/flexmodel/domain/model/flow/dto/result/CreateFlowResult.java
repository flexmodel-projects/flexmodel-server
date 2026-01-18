package dev.flexmodel.domain.model.flow.dto.result;

public class CreateFlowResult extends CommonResult {
  private String flowModuleId;

  public String getFlowModuleId() {
    return flowModuleId;
  }

  public void setFlowModuleId(String flowModuleId) {
    this.flowModuleId = flowModuleId;
  }

  @Override
  public String toString() {
    return "CreateFlowResult{" +
           "flowModuleId='" + flowModuleId + '\'' +
           '}';
  }
}
