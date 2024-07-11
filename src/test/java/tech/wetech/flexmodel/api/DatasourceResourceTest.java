package tech.wetech.flexmodel.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.MySQLTestResource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(MySQLTestResource.class)
class DatasourceResourceTest {

  @Test
  void testValidateConnection() {
    given()
      .when()
      .contentType("application/json")
      .body("""
          {
             "name": "mysql_test",
             "type": "user",
             "config": {
               "url": "${MYSQL_URL}",
               "dbKind": "mysql",
               "password": "${MYSQL_PASSWORD}",
               "username": "${MYSQL_USERNAME}"
             }
           }
        """)
      .post("/api/datasources/validate")
      .then()
      .statusCode(200)
      .body("success", is(true));
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
      .body("size()", greaterThanOrEqualTo(1));

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
