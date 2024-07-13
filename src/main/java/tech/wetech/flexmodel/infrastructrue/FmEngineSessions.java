package tech.wetech.flexmodel.infrastructrue;

import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.FlexmodelConfig;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.domain.model.connect.SessionDatasource;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

import java.util.List;

/**
 * @author cjbi
 */
@Dependent
@Startup
public class FmEngineSessions {

  public static final String SYSTEM_DS_KEY = "system";

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
    dataSource.setMaxLifetime(30000); // 30s
    dataSource.setJdbcUrl(datasourceConfig.url());
    dataSource.setUsername(datasourceConfig.username().orElse(null));
    dataSource.setPassword(datasourceConfig.password().orElse(null));
      return SessionFactory.builder()
      .setDefaultDataSourceProvider(SYSTEM_DS_KEY, new JdbcDataSourceProvider(dataSource))
      .build();
  }

}
