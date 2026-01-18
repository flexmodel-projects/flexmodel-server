package dev.flexmodel.domain.model.flow.dto.bo;

public class FlowInfo extends FlowBasicInfo {
  private String flowModel;

  public String getFlowModel() {
    return flowModel;
  }

  public void setFlowModel(String flowModel) {
    this.flowModel = flowModel;
  }

  @Override
  public String toString() {
    return "FlowInfo{" +
           "flowModel='" + flowModel + '\'' +
           '}';
  }
}
