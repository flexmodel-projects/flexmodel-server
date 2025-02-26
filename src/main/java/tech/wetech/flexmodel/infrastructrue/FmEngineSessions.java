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
    FlexmodelConfig.DatasourceConfig datasourceConfig = flexmodelConfig.datasource();
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setMaxLifetime(30000); // 30s
    dataSource.setJdbcUrl(datasourceConfig.url());
    dataSource.setUsername(datasourceConfig.username().orElse(null));
    dataSource.setPassword(datasourceConfig.password().orElse(null));
    return SessionFactory.builder()
      .setDefaultDataSourceProvider(new JdbcDataSourceProvider(SYSTEM_DS_KEY, dataSource))
      .setFailsafe(true)
      .build();
  }

  @Produces
  @Singleton
  public GraphQLProvider graphQLProvider(SessionFactory sessionFactory) {
    return new GraphQLProvider(sessionFactory);
  }

}
