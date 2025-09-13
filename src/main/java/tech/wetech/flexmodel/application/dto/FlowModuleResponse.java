package tech.wetech.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.codegen.entity.FlowDefinition;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@Getter
@Setter
public class FlowModuleResponse {

  private String flowModuleId;
  private String flowName;
  private String flowKey;
  private Integer status;
  private String remark;
  private String tenant;
  private String caller;
  private String operator;
  private LocalDateTime modifyTime;

  public FlowModuleResponse() {

  }

  public FlowModuleResponse(FlowDefinition flowDefinition) {
    this.flowModuleId = flowDefinition.getFlowModuleId();
    this.flowName = flowDefinition.getFlowName();
    this.flowKey = flowDefinition.getFlowKey();
    this.status = flowDefinition.getStatus();
    this.remark = flowDefinition.getRemark();
    this.tenant = flowDefinition.getTenant();
    this.caller = flowDefinition.getCaller();
    this.operator = flowDefinition.getOperator();
    this.modifyTime = flowDefinition.getModifyTime();
  }

}
