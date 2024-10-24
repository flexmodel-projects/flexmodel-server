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
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.domain.model.connect.SessionDatasource;
import tech.wetech.flexmodel.domain.model.connect.ValidateResult;
import tech.wetech.flexmodel.domain.model.connect.database.Database;
import tech.wetech.flexmodel.domain.model.connect.database.MongoDB;
import tech.wetech.flexmodel.mongodb.MongoDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.util.JsonUtils;
import tech.wetech.flexmodel.util.StringUtils;
import tech.wetech.flexmodel.util.SystemVariablesHolder;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
      DataSourceProvider dataSourceProvider = buildDataSourceProvider(datasource);
      sessionFactory.addDataSourceProvider(datasource.getName(), dataSourceProvider);
    } catch (Exception e) {
      log.error("Session dataSource create error: {}", e.getMessage(), e);
    }
  }

  private DataSourceProvider buildDataSourceProvider(Datasource datasource) {
    DataSourceProvider dataSourceProvider;
    if (datasource.getConfig() instanceof MongoDB mongoDB) {
      dataSourceProvider = new MongoDataSourceProvider(buildMongoDatabase(mongoDB));
    } else {
      Database config = JsonUtils.getInstance().convertValue(datasource.getConfig(), Database.class);
      dataSourceProvider = new JdbcDataSourceProvider(buildJdbcDataSource(config));
    }
    return dataSourceProvider;
  }

  public DataSource buildJdbcDataSource(Database database) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setMaxLifetime(30000); // 30s
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
  public void delete(String datasourceName) {
    sessionFactory.removeDataSourceProvider(datasourceName);
  }

}
