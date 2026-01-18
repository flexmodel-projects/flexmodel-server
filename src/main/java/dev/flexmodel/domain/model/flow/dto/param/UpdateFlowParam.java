package dev.flexmodel.domain.model.flow.dto.param;

public class UpdateFlowParam extends OperationParam {
  private String flowKey;
  private String flowName;
  private String flowModuleId;
  private String flowModel;
  private String remark;

  public UpdateFlowParam(String tenant, String caller) {
    super(tenant, caller);
  }

  public String getFlowKey() {
    return flowKey;
  }

  public void setFlowKey(String flowKey) {
    this.flowKey = flowKey;
  }

  public String getFlowName() {
    return flowName;
  }

  public void setFlowName(String flowName) {
    this.flowName = flowName;
  }

  public String getFlowModuleId() {
    return flowModuleId;
  }

  public void setFlowModuleId(String flowModuleId) {
    this.flowModuleId = flowModuleId;
  }

  public String getFlowModel() {
    return flowModel;
  }

  public void setFlowModel(String flowModel) {
    this.flowModel = flowModel;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  @Override
  public String toString() {
    return "UpdateFlowParam{" +
           "flowKey='" + flowKey + '\'' +
           ", flowName='" + flowName + '\'' +
           ", flowModuleId='" + flowModuleId + '\'' +
           ", flowModel='" + flowModel + '\'' +
           ", remark='" + remark + '\'' +
           '}';
  }
}
