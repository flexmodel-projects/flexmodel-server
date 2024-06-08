package tech.wetech.flexmodel.infrastructrue;

import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.domain.model.api.ApiInfo;
import tech.wetech.flexmodel.domain.model.api.ApiLog;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.generations.DatetimeNowValueGenerator;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcMappedModels;

import java.util.List;

/**
 * @author cjbi
 */
@Dependent
@Startup
public class FmEngineSessions {

  public static final String SYSTEM_DS_KEY = "system";

  @Produces
  public Session session(SessionFactory sessionFactory) {
    return sessionFactory.openSession(SYSTEM_DS_KEY);
  }

  private void createApiInfoEntity(Session session) {
    String apiInfoEntity = ApiInfo.class.getSimpleName();
    if (session.getModel(apiInfoEntity) == null) {
      session.createEntity(apiInfoEntity, entity -> entity
        .addField(new IDField("id").setGeneratedValue(IDField.GeneratedValue.ULID))
        .addField(new StringField("name").setNullable(false))
        .addField(new StringField("parentId"))
        .addField(new StringField("type").setNullable(false).setDefaultValue(ApiInfo.Type.FOLDER.name()))
        .addField(new StringField("method"))
        .addField(new StringField("path"))
        .addField(new DatetimeField("createdAt").setNullable(false).addGenration(new DatetimeNowValueGenerator().setSkipIfNonNull(true)))
        .addField(new DatetimeField("updatedAt").setNullable(false).addGenration(new DatetimeNowValueGenerator()))
        .addField(new JsonField("meta"))
      );
    }
  }

  private void createApiLogEntity(Session session) {
    String apiLogEntity = ApiLog.class.getSimpleName();
    if (session.getModel(apiLogEntity) == null) {
      session.createEntity(apiLogEntity, entity -> entity
        .addField(new IDField("id").setGeneratedValue(IDField.GeneratedValue.ULID))
        .addField(new StringField("level").setNullable(false))
        .addField(new TextField("uri").setNullable(false))
        .addField(new JsonField("data").setNullable(false))
        .addField(new DatetimeField("createdAt").setNullable(false).addGenration(new DatetimeNowValueGenerator().setSkipIfNonNull(true)))
      );
    }
  }

  private void createDatasourceEntity(Session session) {
    String datasourceEntity = Datasource.class.getSimpleName();
    if (session.getModel(datasourceEntity) == null) {
      session.createEntity(datasourceEntity, entity -> entity
        .addField(new IDField("name").setGeneratedValue(IDField.GeneratedValue.STRING_NO_GEN))
        .addField(new StringField("type"))
        .addField(new JsonField("config"))
        .addField(new DatetimeField("createdAt").addGenration(new DatetimeNowValueGenerator().setSkipIfNonNull(true)))
        .addField(new DatetimeField("updatedAt").addGenration(new DatetimeNowValueGenerator()))
      );
      session.insertAll(datasourceEntity, JsonUtils.getInstance().parseToObject("""
        [
          {
            "type": "db",
            "config": {
              "dbKind": "mysql",
              "url": "jdbc:mysql://wetech.tech:3306/flexmodel",
              "username": "root",
              "password": "123456"
            },
            "name": "test_ds_a"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "mariadb",
              "url": "jdbc:mariadb://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_b"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "oracle",
              "url": "jdbc:oracle://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_c"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "sqlserver",
              "url": "jdbc:sqlserver://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_d"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "postgresql",
              "url": "jdbc:postgresql://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_e"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "db2",
              "url": "jdbc:db2://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_f"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "sqlite",
              "url": "jdbc:sqlite://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_g"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "gbase",
              "url": "jdbc:gbase://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_h"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "dm",
              "url": "jdbc:dm://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_i"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "tidb",
              "url": "jdbc:tidb://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_j"
          },
          {
            "type": "db",
            "config": {
              "dbKind": "mongodb",
              "url": "jdbc:mongodb://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_k"
          }
        ]
        """, List.class));
    }
  }


  @Produces
  public SessionFactory sessionFactory(FlexmodelConfig flexmodelConfig, ConnectionLifeCycleManager connectionLifeCycleManager) {
    HikariDataSource dataSource = new HikariDataSource();
    FlexmodelConfig.Datasource datasourceConfig = flexmodelConfig.datasource();
    dataSource.setJdbcUrl(datasourceConfig.url());
    dataSource.setUsername(datasourceConfig.username().orElse(null));
    dataSource.setPassword(datasourceConfig.password().orElse(null));
    dataSource.setConnectionTimeout(3000);
    dataSource.setValidationTimeout(5000);
    connectionLifeCycleManager.addDataSourceProvider(SYSTEM_DS_KEY, new JdbcDataSourceProvider(dataSource));
    SessionFactory sessionFactory = SessionFactory.builder()
      .setConnectionLifeCycleManager(connectionLifeCycleManager)
      .setMappedModels(new JdbcMappedModels(dataSource))
      .build();
    Session session = sessionFactory.openSession(SYSTEM_DS_KEY);
    try {
      createDatasourceEntity(session);
      createApiInfoEntity(session);
      createApiLogEntity(session);

    } catch (Exception ignored) {
    }
    session.syncModels();
    session.close();
    return sessionFactory;
  }

  @Produces
  public ConnectionLifeCycleManager connectionLifeCycleManager() {
    return new ConnectionLifeCycleManager();
  }

}
