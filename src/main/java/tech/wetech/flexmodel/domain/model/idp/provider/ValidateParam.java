package tech.wetech.flexmodel.domain.model.idp.provider;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author cjbi
 */
@Getter
@Setter
public class ValidateParam {
  private Map<String, Object> headers;
  private Map<String, Object> body;
}
