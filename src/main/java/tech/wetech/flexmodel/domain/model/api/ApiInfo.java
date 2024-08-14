package tech.wetech.flexmodel.domain.model.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Getter
@Setter
@ToString
public class ApiInfo {

  private String id;
  @NotBlank
  private String name;
  private String parentId;
  private Type type;
  private String method;
  private String path;
  private Map<String, Object> meta = new HashMap<>();
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public enum Type {
    FOLDER, REST_API,
  }
}
