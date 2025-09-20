package tech.wetech.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.codegen.entity.Trigger;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@Getter
@Setter
public class TriggerDTO extends Trigger {

  private String jobName;
  private LocalDateTime nextFireTime;
  private LocalDateTime previousFireTime;

}
