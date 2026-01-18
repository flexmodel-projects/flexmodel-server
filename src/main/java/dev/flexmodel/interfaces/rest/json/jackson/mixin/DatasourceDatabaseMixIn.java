package dev.flexmodel.interfaces.rest.json.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.flexmodel.domain.model.connect.database.*;
import dev.flexmodel.domain.model.connect.database.*;

/**
 * @author cjbi
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "dbKind",defaultImpl = UnknownDatabase.class)
@JsonSubTypes({
  @JsonSubTypes.Type(value = MySQL.class, name = "mysql"),
  @JsonSubTypes.Type(value = MariaDB.class, name = "mariadb"),
  @JsonSubTypes.Type(value = Oracle.class, name = "oracle"),
  @JsonSubTypes.Type(value = SQLServer.class, name = "sqlserver"),
  @JsonSubTypes.Type(value = PostgreSQL.class, name = "postgresql"),
  @JsonSubTypes.Type(value = DB2.class, name = "db2"),
  @JsonSubTypes.Type(value = SQLite.class, name = "sqlite"),
  @JsonSubTypes.Type(value = GBase.class, name = "gbase"),
  @JsonSubTypes.Type(value = DM8.class, name = "dm"),
  @JsonSubTypes.Type(value = TiDB.class, name = "tidb"),
  @JsonSubTypes.Type(value = MongoDB.class, name = "mongodb"),
})
public class DatasourceDatabaseMixIn {

}
