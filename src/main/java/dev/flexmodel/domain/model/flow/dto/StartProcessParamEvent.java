package dev.flexmodel.domain.model.flow.dto;

import lombok.Getter;
import lombok.Setter;
import dev.flexmodel.domain.model.flow.dto.param.StartProcessParam;

/**
 * @author cjbi
 */
@Setter
@Getter
public class StartProcessParamEvent extends StartProcessParam {

  private String projectId;
  private String userId;
  private String eventId;
  private Long startTime;
}
