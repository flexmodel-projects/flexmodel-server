package tech.wetech.flexmodel.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.MySQLTestResource;

import static io.restassured.RestAssured.given;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(MySQLTestResource.class)
public class ApiLogResourceTest {

  @Test
  void testFindApiList() {
    given()
      .when()
      .get("/api/logs")
      .then()
      .statusCode(200);
  }

  @Test
  void testStat() {
    given()
      .when()
      .get("/api/logs/stat")
      .then()
      .statusCode(200);
  }
}
