package tech.wetech.flexmodel.domain.model.connect;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@Getter
@Setter
public class Datasource {
  private String name;
  private String type;
  private Database config;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Getter
  @Setter
  public abstract static class Database {

    public abstract String getDbKind();

    private String url;
    private String username;
    private String password;

  }

  @Getter
  @Setter
  public static class MySQL extends Database {
    @Override
    public String getDbKind() {
      return "mysql";
    }
  }

  @Getter
  @Setter
  public static class MariaDB extends Database {
    @Override
    public String getDbKind() {
      return "mariadb";
    }
  }

  @Getter
  @Setter
  public static class Oracle extends Database {
    @Override
    public String getDbKind() {
      return "oracle";
    }
  }

  @Getter
  @Setter
  public static class SQLServer extends Database {
    @Override
    public String getDbKind() {
      return "sqlserver";
    }
  }

  @Getter
  @Setter
  public static class PostgreSQL extends Database {
    @Override
    public String getDbKind() {
      return "postgresql";
    }
  }

  @Getter
  @Setter
  public static class DB2 extends Database {
    @Override
    public String getDbKind() {
      return "db2";
    }
  }

  @Getter
  @Setter
  public static class SQLite extends Database {
    private String url;

    @Override
    public String getDbKind() {
      return "sqlite";
    }
  }

  @Getter
  @Setter
  public static class GBase extends Database {
    @Override
    public String getDbKind() {
      return "gbase";
    }
  }

  @Getter
  @Setter
  public static class DM8 extends Database {
    @Override
    public String getDbKind() {
      return "dm";
    }
  }

  @Getter
  @Setter
  public static class TiDB extends Database {
    @Override
    public String getDbKind() {
      return "tidb";
    }
  }

  public static class MongoDB extends Database {
    @Override
    public String getDbKind() {
      return "mongodb";
    }
  }

}
