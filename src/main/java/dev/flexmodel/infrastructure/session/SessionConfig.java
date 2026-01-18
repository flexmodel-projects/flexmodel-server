package dev.flexmodel.infrastructure.session;

import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.application.AuditDataEventListener;
import dev.flexmodel.application.TriggerDataChangedEventListener;
import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.codegen.entity.Project;
import dev.flexmodel.domain.model.auth.ProjectService;
import dev.flexmodel.domain.model.connect.DatasourceService;
import dev.flexmodel.domain.model.connect.SessionDatasource;
import dev.flexmodel.session.SessionFactory;
import dev.flexmodel.shared.FlexmodelConfig;
import dev.flexmodel.sql.JdbcDataSourceProvider;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
@Slf4j
public class SessionConfig {

  public static final String SYSTEM_DS_KEY = "system";

  public void installDatasource(@Observes StartupEvent startupEvent,
                                SessionDatasource sessionDatasource,
                                ProjectService projectService,
                                DatasourceService datasourceService) {
    long beginTime = System.currentTimeMillis();
    List<Project> projects = projectService.findProjects();
    for (Project project : projects) {
      List<Datasource> datasourceList = datasourceService.findAll(project.getId());
      for (Datasource datasource : datasourceList) {
        sessionDatasource.add(datasource);
      }
    }
    log.info("========== Engine init successful in {} ms!", System.currentTimeMillis() - beginTime);
  }

  @Produces
  @ApplicationScoped
  public SessionFactory sessionFactory(FlexmodelConfig flexmodelConfig,
                                       TriggerDataChangedEventListener triggerDataChangedEventListener,
                                       AuditDataEventListener auditDataEventListener) {
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
    SessionFactory sf = builder.build();
    // 添加触发器监听器
    sf.getEventPublisher().addListener(triggerDataChangedEventListener);
    // 添加数据审计监听器
    sf.getEventPublisher().addListener(auditDataEventListener);
    return sf;
  }

}
