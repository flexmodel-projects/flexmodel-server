package tech.wetech.flexmodel.domain.model.flow.dto.param;

import tech.wetech.flexmodel.domain.model.flow.dto.model.InstanceData;

import java.util.List;

public class CommitTaskParam extends RuntimeTaskParam {
  private List<InstanceData> variables;
  // Used to specify the FlowModuleId when commit CallActivity node
  private String callActivityFlowModuleId;

  public List<InstanceData> getVariables() {
    return variables;
  }

  public void setVariables(List<InstanceData> variables) {
    this.variables = variables;
  }

  public String getCallActivityFlowModuleId() {
    return callActivityFlowModuleId;
  }

  public void setCallActivityFlowModuleId(String callActivityFlowModuleId) {
    this.callActivityFlowModuleId = callActivityFlowModuleId;
  }

  @Override
  public String toString() {
    return "CommitTaskParam{" +
           "variables=" + variables +
           ", callActivityFlowModuleId='" + callActivityFlowModuleId + '\'' +
           "} " + super.toString();
  }
}
