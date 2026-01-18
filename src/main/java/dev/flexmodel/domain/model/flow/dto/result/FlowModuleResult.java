package dev.flexmodel.domain.model.flow.dto.result;

import java.time.LocalDateTime;

public class FlowModuleResult extends CommonResult {
  private String flowModuleId;
  private String flowName;
  private String flowKey;
  private String flowModel;
  private Integer status;
  private String remark;
  private String tenant;
  private String caller;
  private String operator;
  private LocalDateTime modifyTime;

  public String getFlowModuleId() {
    return flowModuleId;
  }

  public void setFlowModuleId(String flowModuleId) {
    this.flowModuleId = flowModuleId;
  }

  public String getFlowName() {
    return flowName;
  }

  public void setFlowName(String flowName) {
    this.flowName = flowName;
  }

  public String getFlowKey() {
    return flowKey;
  }

  public void setFlowKey(String flowKey) {
    this.flowKey = flowKey;
  }

  public String getFlowModel() {
    return flowModel;
  }

  public void setFlowModel(String flowModel) {
    this.flowModel = flowModel;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
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

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public LocalDateTime getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(LocalDateTime modifyTime) {
    this.modifyTime = modifyTime;
  }

  @Override
  public String toString() {
    return "FlowModuleResult{" +
           "flowModuleId='" + flowModuleId + '\'' +
           ", flowName='" + flowName + '\'' +
           ", flowKey='" + flowKey + '\'' +
           ", flowModel='" + flowModel + '\'' +
           ", status=" + status +
           ", remark='" + remark + '\'' +
           ", tenant='" + tenant + '\'' +
           ", caller='" + caller + '\'' +
           ", operator='" + operator + '\'' +
           ", modifyTime=" + modifyTime +
           '}';
  }
}
