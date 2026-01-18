package dev.flexmodel.domain.model.connect.database;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class DB2 extends Database {
  @Override
  public String getDbKind() {
    return "db2";
  }
}
