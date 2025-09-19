package tech.wetech.flexmodel.domain.model.trigger;

import tech.wetech.flexmodel.domain.model.BusinessException;

/**
 * @author cjbi
 */
public class TriggerException extends BusinessException {

  public TriggerException(String message) {
    super(message);
  }
}
