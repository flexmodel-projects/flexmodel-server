package tech.wetech.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.codegen.entity.FlowInstance;

/**
 * @author cjbi
 */
@Getter
@Setter
public class FlowInstanceResponse extends FlowInstance {
  private String flowName;
  private String flowKey;

}
