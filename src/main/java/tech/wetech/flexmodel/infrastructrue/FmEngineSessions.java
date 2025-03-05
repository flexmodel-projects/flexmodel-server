package tech.wetech.flexmodel.infrastructrue;

import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.FlexmodelConfig;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.domain.model.connect.SessionDatasource;
import tech.wetech.flexmodel.graphql.GraphQLProvider;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

import java.util.List;

/**
 * @author cjbi
 */
@Dependent
@Startup
@Slf4j
public class FmEngineSessions {

  public static final String SYSTEM_DS_KEY = "system";

  public void installDatasource(@Observes StartupEvent startupEvent,
                                SessionDatasource sessionDatasource,
                                SessionFactory sessionFactory,
                                DatasourceService datasourceService,
                                GraphQLProvider graphQLProvider) {
    List<Datasource> datasourceList = datasourceService.findAll();
    for (Datasource datasource : datasourceList) {
      sessionDatasource.add(datasource);
    }
    graphQLProvider.init();
  }

  @Produces
  @Singleton
  public SessionFactory sessionFactory(FlexmodelConfig flexmodelConfig) {
    FlexmodelConfig.DatasourceConfig datasourceConfig = flexmodelConfig.datasources().get(SYSTEM_DS_KEY);
    HikariDataSource defaultDs = new HikariDataSource();
    defaultDs.setMaxLifetime(30000); // 30s
    defaultDs.setJdbcUrl(datasourceConfig.url());
    defaultDs.setUsername(datasourceConfig.username().orElse(null));
    defaultDs.setPassword(datasourceConfig.password().orElse(null));
    SessionFactory.Builder builder = SessionFactory.builder()
      .setDefaultDataSourceProvider(new JdbcDataSourceProvider(SYSTEM_DS_KEY, defaultDs))
      .setFailsafe(true);
    flexmodelConfig.datasources().forEach((key, value) -> {
      if (key.equals(SYSTEM_DS_KEY)) {
        return;
      }
      HikariDataSource ds = new HikariDataSource();
      ds.setMaxLifetime(30000); // 30s
      ds.setJdbcUrl(value.url());
      ds.setUsername(value.username().orElse(null));
      ds.setPassword(value.password().orElse(null));
      builder.addDataSourceProvider(new JdbcDataSourceProvider(key, ds));
    });
    return builder.build();
  }

  @Produces
  @Singleton
  public GraphQLProvider graphQLProvider(SessionFactory sessionFactory) {
    return new GraphQLProvider(sessionFactory);
  }

}
