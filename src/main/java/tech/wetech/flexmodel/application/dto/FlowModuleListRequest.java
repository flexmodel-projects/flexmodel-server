package tech.wetech.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class FlowModuleListRequest {
  private String flowModuleId;
  private String flowKey;
  private Integer status;
  private String flowName;
  private Integer page;
  private Integer size;

}
