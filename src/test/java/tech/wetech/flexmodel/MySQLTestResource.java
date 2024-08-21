package tech.wetech.flexmodel;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

import java.util.Map;
import java.util.Optional;

/**
 * @author cjbi
 */
public class MySQLTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

  private Optional<String> containerNetworkId;
  private static JdbcDatabaseContainer<?> container;

  @Override
  public Map<String, String> start() {
    container = new MySQLContainer<>("mysql:8.0").withLogConsumer(outputFrame -> {
    });
    // apply the network to the container
    containerNetworkId.ifPresent(container::withNetworkMode);
    // start container before retrieving its URL or other properties
    container.start();
    String jdbcUrl = container.getJdbcUrl();
    if (containerNetworkId.isPresent()) {
      // Replace hostname + port in the provided JDBC URL with the hostname of the Docker container
      // running PostgreSQL and the listening port.
      jdbcUrl = fixJdbcUrl(jdbcUrl);
    }
    return Map.of(
      "flexmodel.datasource.db-kind", "mysql",
      "flexmodel.datasource.url", jdbcUrl,
      "flexmodel.datasource.username", "root",
      "flexmodel.datasource.password", container.getPassword(),
      "MYSQL_URL", jdbcUrl,
      "MYSQL_USERNAME", "root",
      "MYSQL_PASSWORD", container.getPassword());
  }

  private String fixJdbcUrl(String jdbcUrl) {
    // Part of the JDBC URL to replace
    String hostPort = container.getHost() + ':' + container.getMappedPort(MySQLContainer.MYSQL_PORT);

    // Host/IP on the container network plus the unmapped port
    String networkHostPort =
      container.getCurrentContainerInfo().getConfig().getHostName()
      + ':'
      + MySQLContainer.MYSQL_PORT;

    return jdbcUrl.replace(hostPort, networkHostPort);
  }

  @Override
  public void stop() {
    container.stop();
  }

  @Override
  public void setIntegrationTestContext(DevServicesContext context) {
    containerNetworkId = context.containerNetworkId();
  }

  public static JdbcDatabaseContainer<?> container() {
    return container;
  }

}
