package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class FlowInstanceListRequest {
  private String projectId;
  private String flowInstanceId;
  private String flowModuleId;
  private String flowDeployId;
  private Integer page;
  private Integer size;
  private String caller;
  private Integer status;

}
