package tech.wetech.flexmodel.domain.model.flow.dto;

import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.domain.model.flow.dto.param.StartProcessParam;

import java.util.UUID;

/**
 * @author cjbi
 */
@Setter
@Getter
public class StartProcessParamEvent extends StartProcessParam {

  private String tenantId;
  private String userId;
  private String eventId;
  private Long startTime;
}
