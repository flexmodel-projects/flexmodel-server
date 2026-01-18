package dev.flexmodel.domain.model.idp.provider;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author cjbi
 */
@Getter
@Setter
public class ValidateParam {
  private String method;
  private String url;
  private Map<String, String> query;
  private Map<String, String> headers;
}
