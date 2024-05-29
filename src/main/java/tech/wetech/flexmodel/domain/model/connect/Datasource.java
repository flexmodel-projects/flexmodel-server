package tech.wetech.flexmodel.domain.model.connect;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class Datasource {
  private Long id;
  private String type;
  private Object config;

}
