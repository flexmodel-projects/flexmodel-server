package tech.wetech.flexmodel.domain.model.flow.dto.bo;

public class FlowBasicInfo {
  private String flowDeployId;
  private String flowModuleId;
  private String tenantId;
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

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
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
           ", tenant='" + tenantId + '\'' +
           ", caller='" + caller + '\'' +
           '}';
  }
}
