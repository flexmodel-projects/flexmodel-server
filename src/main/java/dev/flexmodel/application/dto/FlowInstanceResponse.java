package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;
import dev.flexmodel.codegen.entity.FlowInstance;

/**
 * @author cjbi
 */
@Getter
@Setter
public class FlowInstanceResponse extends FlowInstance {
  private String flowName;
  private String flowKey;

}
