package tech.wetech.flexmodel.domain.model.connect;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.util.StringUtils;
import tech.wetech.flexmodel.util.SystemVariablesHolder;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@Getter
@Setter
public class Datasource {
  @NotBlank
  private String name;
  private String type;
  @Valid
  private Database config;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Getter
  @Setter
  public abstract static class Database {

    public abstract String getDbKind();

    @NotBlank
    private String url;
    private String username;
    private String password;

    public String urlWithSystemVariables() {
      return StringUtils.simpleRenderTemplate(url, SystemVariablesHolder.getSystemVariables());
    }

    public String usernameWithSystemVariables() {
      return StringUtils.simpleRenderTemplate(username, SystemVariablesHolder.getSystemVariables());
    }

    public String passwordWithSystemVariables() {
      return StringUtils.simpleRenderTemplate(password, SystemVariablesHolder.getSystemVariables());
    }
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
