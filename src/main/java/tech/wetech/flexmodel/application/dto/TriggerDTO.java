package tech.wetech.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.codegen.entity.Trigger;

/**
 * @author cjbi
 */
@Getter
@Setter
public class TriggerDTO extends Trigger {

  private String executorName;

}
