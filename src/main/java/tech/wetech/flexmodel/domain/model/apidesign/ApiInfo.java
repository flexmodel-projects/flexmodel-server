package tech.wetech.flexmodel.domain.model.apidesign;

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
public class ApiInfo {

  private String id;
  private String name;
  private String parentId;
  private Type type;
  private String method;
  private String path;
  private Map<String, Object> meta = new HashMap<>();
  private LocalDateTime createTime;

  public enum Type {
    FOLDER, REST_API
  }
}
