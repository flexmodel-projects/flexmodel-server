package tech.wetech.flexmodel.infrastructrue;

import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.domain.model.api.ApiInfo;
import tech.wetech.flexmodel.domain.model.api.ApiLog;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.domain.model.connect.SessionDatasource;
import tech.wetech.flexmodel.generator.DatetimeNowValueGenerator;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

import java.util.List;

import static tech.wetech.flexmodel.generator.GenerationTime.ALWAYS;
import static tech.wetech.flexmodel.generator.GenerationTime.INSERT;

/**
 * @author cjbi
 */
@Dependent
@Startup
public class FmEngineSessions {

  public static final String SYSTEM_DS_KEY = "system";

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
        .addField(new DatetimeField("createdAt").setNullable(false).setGenerator(new DatetimeNowValueGenerator().setGenerationTime(INSERT)))
        .addField(new DatetimeField("updatedAt").setNullable(false).setGenerator(new DatetimeNowValueGenerator().setGenerationTime(ALWAYS)))
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
        .addField(new DatetimeField("createdAt").setNullable(false).setGenerator(new DatetimeNowValueGenerator().setGenerationTime(INSERT)))
      );
    }
  }

  private void createDatasourceEntity(Session session) {
    String datasourceEntity = Datasource.class.getSimpleName();
    if (session.getModel(datasourceEntity) == null) {
      session.createEntity(datasourceEntity, entity -> entity
        .addField(new IDField("name").setGeneratedValue(IDField.GeneratedValue.STRING_NOT_GENERATED))
        .addField(new StringField("type"))
        .addField(new JsonField("config"))
        .addField(new DatetimeField("createdAt").setGenerator(new DatetimeNowValueGenerator().setGenerationTime(INSERT)))
        .addField(new DatetimeField("updatedAt").setGenerator(new DatetimeNowValueGenerator()))
      );
    }
  }

  public void installDatasource(@Observes StartupEvent startupEvent, SessionDatasource sessionDatasource, SessionFactory sessionFactory) {
    try (Session session = sessionFactory.createSession(SYSTEM_DS_KEY)) {
      List<Datasource> datasourceList = session.find(Datasource.class.getSimpleName(), query -> query, Datasource.class);
      for (Datasource datasource : datasourceList) {
        sessionDatasource.add(datasource);
      }
    }
  }


  @Produces
  @Singleton
  public SessionFactory sessionFactory(FlexmodelConfig flexmodelConfig) {
    FlexmodelConfig.DatasourceConfig datasourceConfig = flexmodelConfig.datasource();
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setMaximumPoolSize(100);
    dataSource.setMaxLifetime(30000); // 30s
    dataSource.setJdbcUrl(datasourceConfig.url());
    dataSource.setUsername(datasourceConfig.username().orElse(null));
    dataSource.setPassword(datasourceConfig.password().orElse(null));
    dataSource.setConnectionTimeout(3000);
    dataSource.setValidationTimeout(5000);
    SessionFactory sessionFactory = SessionFactory.builder()
      .setDefaultDataSourceProvider(SYSTEM_DS_KEY, new JdbcDataSourceProvider(dataSource))
      .build();
    try (Session session = sessionFactory.createSession(SYSTEM_DS_KEY)) {
      createDatasourceEntity(session);
      createApiInfoEntity(session);
      createApiLogEntity(session);
      session.syncModels();
    } catch (Exception ignored) {
    }
    return sessionFactory;
  }

}
