package dev.flexmodel.domain.model.connect.database;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public abstract class Database {

  public abstract String getDbKind();

  @NotBlank
  private String url;
  private String username;
  private String password;
}
