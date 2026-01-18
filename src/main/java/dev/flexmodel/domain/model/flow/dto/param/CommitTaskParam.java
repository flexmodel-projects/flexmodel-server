package dev.flexmodel.domain.model.flow.dto.param;

import java.util.Map;

public class CommitTaskParam extends RuntimeTaskParam {
  private Map<String, Object> variables;
  // Used to specify the FlowModuleId when commit CallActivity node
  private String callActivityFlowModuleId;

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
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
