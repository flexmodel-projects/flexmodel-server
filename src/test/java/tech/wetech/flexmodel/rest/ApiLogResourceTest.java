package dev.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;

import static io.restassured.RestAssured.given;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class ApiLogResourceTest {

  @Test
  void testFindApiLogs() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(Resources.ROOT_PATH + "/projects/dev_test/logs")
      .then()
      .statusCode(200);
  }

  @Test
  void testStat() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(Resources.ROOT_PATH + "/projects/dev_test/logs/stat")
      .then()
      .statusCode(200);
  }
}
