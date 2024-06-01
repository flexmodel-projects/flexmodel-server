package tech.wetech.flexmodel.infrastructrue;

import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.IDField.DefaultGeneratedValue;
import tech.wetech.flexmodel.calculations.DatetimeNowValueCalculator;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcMappedModels;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author cjbi
 */
@Dependent
@Startup
public class EngineConfig {

  public static final String SYSTEM_DS_KEY = "system";

  @Produces
  public Session session(SessionFactory sessionFactory) {
    Session session = sessionFactory.createSession(SYSTEM_DS_KEY);
    session.syncModels();
    if (session.getModel("Student") == null) {
      session.createEntity("Student", entity -> entity
        .addField(new IDField("id").setGeneratedValue(DefaultGeneratedValue.IDENTITY))
        .addField(new StringField("name"))
      );
    }
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
            "type": "mysql",
            "config": {
              "dbKind": "mysql",
              "url": "jdbc:mysql://wetech.tech:3306/flexmodel",
              "username": "root",
              "password": "123456"
            },
            "name": "test_ds_a"
          },
          {
            "type": "oracle",
            "config": {
              "dbKind": "oracle",
              "url": "jdbc:oracle://wetech.tech:3306/flexmodel",
              "username": "sa",
              "password": "sdfweerwe"
            },
            "name": "test_ds_b"
          }
        ]
        """, List.class));
    }
    session.insert("Student", Map.of("name", "测试" + new Random().nextInt()));
    return session;
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
