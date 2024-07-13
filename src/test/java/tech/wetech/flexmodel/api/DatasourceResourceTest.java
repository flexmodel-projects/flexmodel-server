package tech.wetech.flexmodel.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
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
      .contentType(ContentType.JSON)
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
    given()
      .when()
      .get("/api/datasources/system/refresh")
      .then()
      .statusCode(200)
      .body("size()", greaterThanOrEqualTo(1));
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
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
             "name": "mysql_test2",
             "type": "user",
             "config": {
               "url": "${MYSQL_URL}",
               "dbKind": "mysql",
               "password": "${MYSQL_PASSWORD}",
               "username": "${MYSQL_USERNAME}"
             }
          }
        """)
      .post("/api/datasources")
      .then()
      .statusCode(200);
  }

  @Test
  void testUpdateDatasource() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "name": "sqlite_test",
            "type": "user",
            "config": {
              "url": "jdbc:sqlite:file::memory:?cache=shared",
              "dbKind": "sqlite",
              "password": "",
              "username": ""
            }
          }
        """)
      .put("/api/datasources/{datasourceName}", "sqlite_test")
      .then()
      .statusCode(200);
  }

  @Test
  void testDeleteDatasource() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
             "name": "mysql_test3",
             "type": "user",
             "config": {
               "url": "${MYSQL_URL}",
               "dbKind": "mysql",
               "password": "${MYSQL_PASSWORD}",
               "username": "${MYSQL_USERNAME}"
             }
           }
        """)
      .post("/api/datasources")
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .delete("/api/datasources/{datasourceName}", "mysql_test3")
      .then()
      .statusCode(204);
  }

}
