package tech.wetech.flexmodel.domain.model.flow.dto.bo;

public class FlowBasicInfo {
  private String flowDeployId;
  private String flowModuleId;
  private String tenant;
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

  public String getTenant() {
    return tenant;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
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
           ", tenant='" + tenant + '\'' +
           ", caller='" + caller + '\'' +
           '}';
  }
}
