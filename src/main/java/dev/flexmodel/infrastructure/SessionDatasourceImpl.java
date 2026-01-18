package dev.flexmodel.infrastructure;

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
import dev.flexmodel.DataSourceProvider;
import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.domain.model.connect.NativeQueryResult;
import dev.flexmodel.domain.model.connect.SessionDatasource;
import dev.flexmodel.domain.model.connect.ValidateResult;
import dev.flexmodel.domain.model.connect.database.Database;
import dev.flexmodel.domain.model.connect.database.MongoDB;
import dev.flexmodel.mongodb.MongoDataSourceProvider;
import dev.flexmodel.session.Session;
import dev.flexmodel.session.SessionFactory;
import dev.flexmodel.shared.SystemVariablesHolder;
import dev.flexmodel.shared.utils.JsonUtils;
import dev.flexmodel.shared.utils.StringUtils;
import dev.flexmodel.sql.JdbcDataSourceProvider;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

  private String getContent(String template) {
    return StringUtils.simpleRenderTemplate(template, SystemVariablesHolder.getSystemVariables());
  }

  @Override
  public List<String> getPhysicsModelNames(Datasource datasource) {
    List<String> list = new ArrayList<>();
    if (datasource.getConfig() instanceof MongoDB mongoDB) {
      try (MongoClient mongoClient = MongoClients.create(mongoDB.getUrl())) {
        mongoClient.getClusterDescription();
        mongoClient.getDatabase(datasource.getName()).listCollectionNames()
          .forEach(list::add);
        return list;
      }
    } else {
      Database config = JsonUtils.getInstance().convertValue(datasource.getConfig(), Database.class);
      try (var conn = DriverManager.getConnection(
        getContent(config.getUrl()),
        getContent(config.getUsername()),
        getContent(config.getPassword()))) {
        ResultSet tables = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
        while (tables.next()) {
          String tableName = tables.getString("TABLE_NAME");
          list.add(tableName);
        }
        return list;
      } catch (SQLException e) {
        return list;
      }
    }
  }

  @Override
  public ValidateResult validate(Datasource datasource) {
    long beginTime = System.currentTimeMillis();
    if (datasource.getConfig() instanceof MongoDB mongoDB) {
      try (MongoClient mongoClient = MongoClients.create(mongoDB.getUrl())) {
        mongoClient.getClusterDescription();
        return new ValidateResult(true, "ok", System.currentTimeMillis() - beginTime);
      }
    } else {
      Database config = JsonUtils.getInstance().convertValue(datasource.getConfig(), Database.class);
      try (var conn = DriverManager.getConnection(
        getContent(config.getUrl()),
        getContent(config.getUsername()),
        getContent(config.getPassword()))) {
        return new ValidateResult(conn.isValid(3), "ok", System.currentTimeMillis() - beginTime);
      } catch (SQLException e) {
        return new ValidateResult(false, e.getMessage(), System.currentTimeMillis() - beginTime);
      }
    }
  }

  @Override
  public void add(Datasource datasource) {
    try {
      DataSourceProvider dataSourceProvider = buildDataSourceProvider(datasource.getName(), datasource);
      sessionFactory.addDataSourceProvider(dataSourceProvider);
    } catch (Exception e) {
      log.error("Session dataSource create error: {}", e.getMessage(), e);
    }
  }

  private DataSourceProvider buildDataSourceProvider(String id, Datasource datasource) {
    DataSourceProvider dataSourceProvider;
    if (datasource.getConfig() instanceof MongoDB mongoDB) {
      dataSourceProvider = new MongoDataSourceProvider(id, buildMongoDatabase(mongoDB));
    } else {
      Database config = JsonUtils.getInstance().convertValue(datasource.getConfig(), Database.class);
      dataSourceProvider = new JdbcDataSourceProvider(id, buildJdbcDataSource(config));
    }
    return dataSourceProvider;
  }

  public DataSource buildJdbcDataSource(Database database) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setMaxLifetime(30000); // 30s
//    dataSource.setMaximumPoolSize(30);
    dataSource.setJdbcUrl(getContent(database.getUrl()));
    dataSource.setUsername(getContent(database.getUsername()));
    dataSource.setPassword(getContent(database.getPassword()));
    return dataSource;
  }

  public MongoDatabase buildMongoDatabase(MongoDB mongodb) {
    CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
      fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    MongoClient mongoClient = MongoClients.create(getContent(mongodb.getUrl()));
    return mongoClient.getDatabase("test")
      .withCodecRegistry(pojoCodecRegistry);
  }

  @Override
  public void delete(String projectId, String datasourceName) {
    sessionFactory.removeDataSourceProvider(datasourceName);
  }

  @Override
  @SuppressWarnings("all")
  public NativeQueryResult executeNativeQuery(String projectId, String datasourceName, String statement, Map<String, Object> parameters) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      long beginTime = System.currentTimeMillis();
      Object result = session.data().executeNativeStatement(statement, parameters);
      long endTime = System.currentTimeMillis() - beginTime;
      return new NativeQueryResult(endTime, result);
    }

  }

}
