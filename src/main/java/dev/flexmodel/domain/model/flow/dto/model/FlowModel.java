package dev.flexmodel.domain.model.flow.dto.model;

import java.util.List;

public class FlowModel {
  private List<FlowElement> flowElementList;

  public List<FlowElement> getFlowElementList() {
    return flowElementList;
  }

  public void setFlowElementList(List<FlowElement> flowElementList) {
    this.flowElementList = flowElementList;
  }

  @Override
  public String toString() {
    return "FlowModel{" +
           "flowElementList=" + flowElementList +
           '}';
  }
}
