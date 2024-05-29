package tech.wetech.flexmodel.infrastructrue;

import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

import java.util.Map;

/**
 * @author cjbi
 */
@Dependent
@Startup
public class EngineConfig {

  @Produces
  public Session session(SessionFactory sessionFactory) {
    Session session = sessionFactory.createSession("default");
    if (session.getModel("Student") == null) {
      session.createEntity("Student", entity -> entity
        .addField(new IDField("id").setGeneratedValue(IDField.DefaultGeneratedValue.STRING_NO_GEN))
        .addField(new StringField("name"))
      );
    }
    String datasourceEntity = Datasource.class.getSimpleName();
    if (session.getModel(datasourceEntity) == null) {
      session.createEntity(datasourceEntity, entity -> entity
        .addField(new IDField("id"))
        .addField(new StringField("type"))
        .addField(new JsonField("config"))
      );
      session.insert(datasourceEntity, Map.of("type", "mysql", "config",
        Map.of("host", "127.0.0.1", "username", "root", "password", "123456")));
      session.insert(datasourceEntity, Map.of("type", "oracle", "config",
        Map.of("host", "127.0.0.1", "username", "root", "password", "123456")));
    }
    session.insert("Student", Map.of("id", "001", "name", "张三"));
    return session;
  }


  @Produces
  public SessionFactory sessionFactory(ConnectionLifeCycleManager connectionLifeCycleManager) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
    connectionLifeCycleManager.addDataSourceProvider("default", new JdbcDataSourceProvider(dataSource));
    return SessionFactory.builder()
      .setConnectionLifeCycleManager(connectionLifeCycleManager)
      .setMappedModels(new MapMappedModels())
      .build();
  }

  @Produces
  public ConnectionLifeCycleManager connectionLifeCycleManager() {
    return new ConnectionLifeCycleManager();
  }

}
