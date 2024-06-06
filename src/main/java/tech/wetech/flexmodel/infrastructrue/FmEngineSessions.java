package tech.wetech.flexmodel.infrastructrue;

import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.IDField.DefaultGeneratedValue;
import tech.wetech.flexmodel.calculations.DatetimeNowValueCalculator;
import tech.wetech.flexmodel.domain.model.apidesign.ApiInfo;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcMappedModels;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author cjbi
 */
@Dependent
@Startup
public class FmEngineSessions {

  public static final String SYSTEM_DS_KEY = "system";

  @Produces
  public Session session(SessionFactory sessionFactory) {
    Session session = sessionFactory.createSession(SYSTEM_DS_KEY);
    session.syncModels();
    createDatasourceEntity(session);
    createApiEntity(session);
    return session;
  }

  private void createApiEntity(Session session) {
    String apiEntity = ApiInfo.class.getSimpleName();
    if (session.getModel(apiEntity) == null) {
      session.createEntity(apiEntity, entity -> entity
        .addField(new IDField("id").setGeneratedValue(DefaultGeneratedValue.ULID))
        .addField(new StringField("name").setNullable(false))
        .addField(new StringField("parentId"))
        .addField(new StringField("type").setNullable(false).setDefaultValue(ApiInfo.Type.FOLDER.name()))
        .addField(new StringField("method"))
        .addField(new StringField("path"))
        .addField(new DatetimeField("createTime").setNullable(false).addCalculation(new DatetimeNowValueCalculator()))
        .addField(new JsonField("meta"))
      );
      AtomicReference<String> folderId = new AtomicReference<>();
      session.insert(apiEntity, JsonUtils.getInstance().parseToObject("""
        {
          "name": "分组一",
          "type": "FOLDER"
        }
        """, Map.class), id -> folderId.set((String) id));
      session.insert(apiEntity, JsonUtils.getInstance().parseToObject(String.format("""
        {
          "name": "简单查询接口",
          "parentId": "%s",
          "type": "REST_API",
          "method":"GET",
          "path":"/hello"
        }
        """, folderId), Map.class));
      session.insert(apiEntity, JsonUtils.getInstance().parseToObject(String.format("""
        {
          "name": "简单添加接口",
          "parentId": "%s",
          "type": "REST_API",
          "method":"POST",
          "path":"/hello/post"
        }
        """, folderId), Map.class));
      session.insert(apiEntity, JsonUtils.getInstance().parseToObject(String.format("""
        {
          "name": "简单更新接口",
          "parentId": "%s",
          "type": "REST_API",
          "method":"PUT",
          "path":"/hello/put"
        }
        """, folderId), Map.class));
      session.insert(apiEntity, JsonUtils.getInstance().parseToObject(String.format("""
        {
          "name": "简单删除接口",
          "parentId": "%s",
          "type": "REST_API",
          "method":"DELETE",
          "path":"/hello/delete"
        }
        """, folderId), Map.class));
    }

  }

  private void createDatasourceEntity(Session session) {
    String datasourceEntity = Datasource.class.getSimpleName();
    if (session.getModel(datasourceEntity) == null) {
      session.createEntity(datasourceEntity, entity -> entity
        .addField(new IDField("name").setGeneratedValue(DefaultGeneratedValue.STRING_NO_GEN))
        .addField(new StringField("type"))
        .addField(new JsonField("config"))
        .addField(new DatetimeField("createTime").addCalculation(new DatetimeNowValueCalculator()))
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
    connectionLifeCycleManager.addDataSourceProvider(SYSTEM_DS_KEY, new JdbcDataSourceProvider(dataSource));
    return SessionFactory.builder()
      .setConnectionLifeCycleManager(connectionLifeCycleManager)
      .setMappedModels(new JdbcMappedModels(dataSource))
      .build();
  }

  @Produces
  public ConnectionLifeCycleManager connectionLifeCycleManager() {
    return new ConnectionLifeCycleManager();
  }

}
