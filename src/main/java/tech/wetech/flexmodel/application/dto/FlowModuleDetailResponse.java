package tech.wetech.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.codegen.entity.FlowDefinition;

/**
 * @author cjbi
 */
@Getter
@Setter
public class FlowModuleDetailResponse extends FlowModuleResponse {
  private String flowModel;

  public FlowModuleDetailResponse(FlowDefinition flowDefinition) {
    super(flowDefinition);
    this.flowModel = flowDefinition.getFlowModel();
  }
}
