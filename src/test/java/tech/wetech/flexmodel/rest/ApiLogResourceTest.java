package tech.wetech.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.SQLiteTestResource;

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
      .get(Resources.ROOT_PATH + "/logs")
      .then()
      .statusCode(200);
  }

  @Test
  void testStat() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(Resources.ROOT_PATH + "/logs/stat")
      .then()
      .statusCode(200);
  }
}
