package tech.wetech.flexmodel.domain.model.flow.dto;

import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.domain.model.flow.dto.param.StartProcessParam;

/**
 * @author cjbi
 */
@Setter
@Getter
public class StartProcessParamEvent extends StartProcessParam {

  private String tenantId;
  private String userId;

  private String logId;
  private Long startTime;

}
