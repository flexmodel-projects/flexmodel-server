package tech.wetech.flexmodel.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author cjbi
 */
@QuarkusTest
class DatasourceResourceTest {

  @Test
  void testValidateConnection() {
  }

  @Test
  void testRefresh() {
  }

  @Test
  void testFindAll() {
    given()
      .when()
      .get("/api/datasources")
      .then()
      .statusCode(200)
      .body("size()", is(2));

  }

  @Test
  void testCreateDatasource() {
  }

  @Test
  void testUpdateDatasource() {
  }

  @Test
  void testDeleteDatasource() {
  }

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

}
