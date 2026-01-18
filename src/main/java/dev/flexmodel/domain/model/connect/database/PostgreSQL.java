package dev.flexmodel.domain.model.connect.database;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class PostgreSQL extends Database {
  @Override
  public String getDbKind() {
    return "postgresql";
  }
}
