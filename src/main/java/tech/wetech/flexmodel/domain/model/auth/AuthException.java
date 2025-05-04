package tech.wetech.flexmodel.domain.model.auth;

import tech.wetech.flexmodel.domain.model.BusinessException;

/**
 * @author cjbi
 */
public class AuthException extends BusinessException {

  public AuthException(String message) {
    super(message);
  }

  public AuthException(String message, Throwable cause) {
    super(message, cause);
  }
}
