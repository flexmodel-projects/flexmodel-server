package tech.wetech.flexmodel.domain.model.connect.database;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class MySQL extends Database {
  @Override
  public String getDbKind() {
    return "mysql";
  }
}