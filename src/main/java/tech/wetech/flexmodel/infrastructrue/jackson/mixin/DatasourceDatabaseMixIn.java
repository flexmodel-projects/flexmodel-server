package tech.wetech.flexmodel.infrastructrue.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.wetech.flexmodel.domain.model.connect.Datasource;

/**
 * @author cjbi
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "dbKind")
@JsonSubTypes({
  @JsonSubTypes.Type(value = Datasource.MySQL.class, name = "mysql"),
  @JsonSubTypes.Type(value = Datasource.MariaDB.class, name = "mariadb"),
  @JsonSubTypes.Type(value = Datasource.Oracle.class, name = "oracle"),
  @JsonSubTypes.Type(value = Datasource.SQLServer.class, name = "sqlserver"),
  @JsonSubTypes.Type(value = Datasource.PostgreSQL.class, name = "postgresql"),
  @JsonSubTypes.Type(value = Datasource.DB2.class, name = "db2"),
  @JsonSubTypes.Type(value = Datasource.SQLite.class, name = "sqlite"),
  @JsonSubTypes.Type(value = Datasource.GBase.class, name = "gbase"),
  @JsonSubTypes.Type(value = Datasource.DM8.class, name = "dm"),
  @JsonSubTypes.Type(value = Datasource.TiDB.class, name = "tidb"),
  @JsonSubTypes.Type(value = Datasource.MongoDB.class, name = "mongodb"),
})
public class DatasourceDatabaseMixIn {

}
