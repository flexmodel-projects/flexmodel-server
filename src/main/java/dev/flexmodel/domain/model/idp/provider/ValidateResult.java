package dev.flexmodel.domain.model.idp.provider;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class ValidateResult {
  private boolean success;
  private String message;

  public ValidateResult(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

}
