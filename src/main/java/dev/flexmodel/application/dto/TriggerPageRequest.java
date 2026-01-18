package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class TriggerPageRequest {
  private String projectId;
  private String name;
  private String jobType;
  private String jobId;
  private String jobGroup;
  private Integer page = 1;
  private Integer size = 15;
}
