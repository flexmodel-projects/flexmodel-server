package dev.flexmodel.domain.model.flow.dto.result;

import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;

import java.io.Serializable;

public class CommonResult implements Serializable {

  private String projectId;
  private int errCode;
  private String errMsg;

  public CommonResult() {
    super();
  }

  public CommonResult(ErrorEnum errorEnum) {
    this.errCode = errorEnum.getErrNo();
    this.errMsg = errorEnum.getErrMsg();
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public int getErrCode() {
    return errCode;
  }

  public void setErrCode(int errCode) {
    this.errCode = errCode;
  }

  public String getErrMsg() {
    return errMsg;
  }

  public void setErrMsg(String errMsg) {
    this.errMsg = errMsg;
  }

  @Override
  public String toString() {
    return "CommonResult{" +
           "errCode=" + errCode +
           ", errMsg='" + errMsg + '\'' +
           '}';
  }
}
