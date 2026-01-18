package dev.flexmodel.domain.model.flow.event;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author cjbi
 */
@Getter
@Setter
public class StartFlowEvent {
  private String flowDeployId;
  private String flowModuleId;
  private Map<String, Object> variables;
}
