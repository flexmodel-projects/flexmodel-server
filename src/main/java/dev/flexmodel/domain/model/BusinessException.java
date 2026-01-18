package dev.flexmodel.domain.model;

/**
 * @author cjbi
 */
public abstract class BusinessException extends RuntimeException {

  public BusinessException(String message) {
    super(message);
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }
}
