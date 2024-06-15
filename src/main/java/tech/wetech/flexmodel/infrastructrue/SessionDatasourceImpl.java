package tech.wetech.flexmodel.infrastructrue;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import tech.wetech.flexmodel.DataSourceProvider;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.domain.model.connect.SessionDatasource;
import tech.wetech.flexmodel.domain.model.connect.ValidateResult;
import tech.wetech.flexmodel.mongodb.MongoDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * @author cjbi
 */
@ApplicationScoped
@Slf4j
public class SessionDatasourceImpl implements SessionDatasource {

  @Inject
  SessionFactory sessionFactory;

  @Override
  public ValidateResult validate(Datasource datasource) {
    long beginTime = System.currentTimeMillis();
    if (datasource.getConfig() instanceof Datasource.MongoDB mongoDB) {
      try (MongoClient mongoClient = MongoClients.create(mongoDB.getUrl())) {
        mongoClient.getClusterDescription();
        return new ValidateResult(true, null, System.currentTimeMillis() - beginTime);
      }
    } else {
      Datasource.Database config = datasource.getConfig();
      try (var conn = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword())) {
        return new ValidateResult(conn.isValid(3), null, System.currentTimeMillis() - beginTime);
      } catch (SQLException e) {
        return new ValidateResult(false, e.getMessage(), System.currentTimeMillis() - beginTime);
      }
    }
  }

  @Override
  public void add(Datasource datasource) {
    try {
      DataSourceProvider dataSourceProvider = buildDataSourceProvider(datasource);
      sessionFactory.addDataSourceProvider(datasource.getName(), dataSourceProvider);
    } catch (Exception e) {
      log.error("Session dataSource create error: {}", e.getMessage(), e);
    }
  }

  private DataSourceProvider buildDataSourceProvider(Datasource datasource) {
    DataSourceProvider dataSourceProvider;
    if (datasource.getConfig() instanceof Datasource.MongoDB mongoDB) {
      dataSourceProvider = new MongoDataSourceProvider(buildMongoDatabase(mongoDB));
    } else {
      dataSourceProvider = new JdbcDataSourceProvider(buildJdbcDataSource(datasource.getConfig()));
    }
    return dataSourceProvider;
  }

  public DataSource buildJdbcDataSource(Datasource.Database database) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setMaximumPoolSize(100);
    dataSource.setMaxLifetime(30000); // 30s
    dataSource.setJdbcUrl(database.getUrl());
    dataSource.setUsername(database.getUsername());
    dataSource.setPassword(database.getPassword());
    return dataSource;
  }

  public MongoDatabase buildMongoDatabase(Datasource.MongoDB mongodb) {
    CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
      fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    MongoClient mongoClient = MongoClients.create(mongodb.getUrl());
    return mongoClient.getDatabase("test")
      .withCodecRegistry(pojoCodecRegistry);
  }

  @Override
  public void delete(String datasourceName) {
    sessionFactory.removeDataSourceProvider(datasourceName);
  }
}
