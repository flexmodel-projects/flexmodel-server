package dev.flexmodel.domain.model.flow.dto.param;

public class DeployFlowParam extends OperationParam {
  private String flowModuleId;

  public DeployFlowParam(String tenant, String caller) {
    super(tenant, caller);
  }

  public String getFlowModuleId() {
    return flowModuleId;
  }

  public void setFlowModuleId(String flowModuleId) {
    this.flowModuleId = flowModuleId;
  }

  @Override
  public String toString() {
    return "DeployFlowParam{" +
           "flowModuleId='" + flowModuleId + '\'' +
           '}';
  }
}
