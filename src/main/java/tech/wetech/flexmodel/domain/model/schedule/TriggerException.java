package tech.wetech.flexmodel.domain.model.schedule;

import tech.wetech.flexmodel.domain.model.BusinessException;

/**
 * @author cjbi
 */
public class TriggerException extends BusinessException {

  public TriggerException(String message) {
    super(message);
  }

  public TriggerException(String message, Throwable cause) {
    super(message, cause);
  }
}
