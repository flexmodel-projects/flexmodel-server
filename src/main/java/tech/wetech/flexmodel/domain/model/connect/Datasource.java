package tech.wetech.flexmodel.domain.model.connect;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Getter
@Setter
public class Datasource {
  private String id;
  private String name;
  private String type;
  private Map<String, Object> config = new HashMap<>();
  private LocalDateTime createTime;

}
