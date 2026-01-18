package dev.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;
import dev.flexmodel.domain.model.settings.Settings;
import dev.flexmodel.interfaces.rest.jwt.JwtUtil;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class SettingsResourceTest {

  /**
   * 获取测试用的token
   */
  private String getTestToken() {
    return JwtUtil.sign("admin", Duration.ofMinutes(5));
  }

  @Test
  void testGetSettings() {
    // 先重置为默认设置
    Settings defaultSettings = new Settings();
    defaultSettings.setAppName("Flexmodel");

    Settings.Log log = new Settings.Log();
    log.setMaxDays(7);
    log.setConsoleLoggingEnabled(true);
    defaultSettings.setLog(log);

    Settings.Security security = new Settings.Security();
    security.setRateLimitingEnabled(false);
    security.setMaxRequestCount(500);
    security.setIntervalInSeconds(60);
    security.setGraphqlEndpointPath("/graphql");
    defaultSettings.setSecurity(security);

    Settings.Proxy proxy = new Settings.Proxy();
    proxy.setRoutesEnabled(false);
    defaultSettings.setProxy(proxy);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType("application/json")
      .body(defaultSettings)
      .when()
      .patch(Resources.ROOT_PATH + "/settings")
      .then()
      .statusCode(200);

    // 然后获取设置并验证
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(Resources.ROOT_PATH + "/settings")
      .then()
      .statusCode(200)
      .body("appName", equalTo("Flexmodel"))
      .body("log.maxDays", equalTo(7))
      .body("log.consoleLoggingEnabled", equalTo(true))
      .body("security.rateLimitingEnabled", equalTo(false))
      .body("security.maxRequestCount", equalTo(500))
      .body("security.intervalInSeconds", equalTo(60))
      .body("security.graphqlEndpointPath", equalTo("/graphql"))
      .body("proxy.routesEnabled", equalTo(false));
  }

  @Test
  void testSaveSettings() {
    Settings settings = new Settings();
    settings.setAppName("TestApp");

    Settings.Log log = new Settings.Log();
    log.setMaxDays(30);
    log.setConsoleLoggingEnabled(false);
    settings.setLog(log);

    Settings.Security security = new Settings.Security();
    security.setRateLimitingEnabled(true);
    security.setMaxRequestCount(1000);
    security.setIntervalInSeconds(120);
    security.setGraphqlEndpointPath("/api/graphql");
    settings.setSecurity(security);

    Settings.Proxy proxy = new Settings.Proxy();
    proxy.setRoutesEnabled(true);
    settings.setProxy(proxy);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType("application/json")
      .body(settings)
      .when()
      .patch(Resources.ROOT_PATH + "/settings")
      .then()
      .statusCode(200)
      .body("appName", equalTo("TestApp"))
      .body("log.maxDays", equalTo(30))
      .body("log.consoleLoggingEnabled", equalTo(false))
      .body("security.rateLimitingEnabled", equalTo(true))
      .body("security.maxRequestCount", equalTo(1000))
      .body("security.intervalInSeconds", equalTo(120))
      .body("security.graphqlEndpointPath", equalTo("/api/graphql"))
      .body("proxy.routesEnabled", equalTo(true));
  }

  @Test
  void testSaveSettingsWithPartialData() {
    Settings settings = new Settings();
    settings.setAppName("PartialTestApp");

    // 只设置部分字段
    Settings.Log log = new Settings.Log();
    log.setMaxDays(15);
    settings.setLog(log);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType("application/json")
      .body(settings)
      .when()
      .patch(Resources.ROOT_PATH + "/settings")
      .then()
      .statusCode(200)
      .body("appName", equalTo("PartialTestApp"))
      .body("log.maxDays", equalTo(15))
      .body("log.consoleLoggingEnabled", equalTo(true)) // 默认值保持不变
      .body("security.rateLimitingEnabled", equalTo(false)) // 默认值保持不变
      .body("proxy.routesEnabled", equalTo(false)); // 默认值保持不变
  }

  @Test
  void testSaveSettingsWithRoutes() {
    Settings settings = new Settings();
    settings.setAppName("RoutesTestApp");

    Settings.Proxy proxy = new Settings.Proxy();
    proxy.setRoutesEnabled(true);

    Settings.Route route1 = new Settings.Route();
    route1.setPath("/api/test1");
    route1.setTo("http://localhost:8081");

    Settings.Route route2 = new Settings.Route();
    route2.setPath("/api/test2");
    route2.setTo("http://localhost:8082");

    proxy.getRoutes().add(route1);
    proxy.getRoutes().add(route2);
    settings.setProxy(proxy);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType("application/json")
      .body(settings)
      .when()
      .patch(Resources.ROOT_PATH + "/settings")
      .then()
      .statusCode(200)
      .body("appName", equalTo("RoutesTestApp"))
      .body("proxy.routesEnabled", equalTo(true))
      .body("proxy.routes.size()", equalTo(2))
      .body("proxy.routes[0].path", equalTo("/api/test1"))
      .body("proxy.routes[0].to", equalTo("http://localhost:8081"))
      .body("proxy.routes[1].path", equalTo("/api/test2"))
      .body("proxy.routes[1].to", equalTo("http://localhost:8082"));
  }

  @Test
  void testGetSettingsAfterSave() {
    // 先保存设置
    Settings settings = new Settings();
    settings.setAppName("PersistentTestApp");

    Settings.Log log = new Settings.Log();
    log.setMaxDays(25);
    settings.setLog(log);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType("application/json")
      .body(settings)
      .when()
      .patch(Resources.ROOT_PATH + "/settings")
      .then()
      .statusCode(200);

    // 然后获取设置，验证数据持久化
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(Resources.ROOT_PATH + "/settings")
      .then()
      .statusCode(200)
      .body("appName", equalTo("PersistentTestApp"))
      .body("log.maxDays", equalTo(25));
  }
}
