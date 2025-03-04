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
             "type": "USER",
             "config": {
               "url": "${MYSQL_URL}",
               "dbKind": "mysql",
               "password": "${MYSQL_PASSWORD}",
               "username": "${MYSQL_USERNAME}"
             }
           }
        """)
      .post(Resources.BASE_PATH + "/datasources/validate")
      .then()
      .statusCode(200)
      .body("success", is(true));
  }

  /*@Test
  void testSync() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        [
          "system_Teacher", "system_Student", "system_Classes"
        ]
        """)
      .post(Resources.BASE_PATH + "/datasources/system/sync")
      .then()
      .statusCode(200)
      .body("size()", greaterThanOrEqualTo(1));
  }*/

  @Test
  void testFindAll() {
    given()
      .when()
      .get(Resources.BASE_PATH + "/datasources")
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
             "type": "USER",
             "config": {
               "url": "${MYSQL_URL}",
               "dbKind": "mysql",
               "password": "${MYSQL_PASSWORD}",
               "username": "${MYSQL_USERNAME}"
             }
          }
        """)
      .post(Resources.BASE_PATH + "/datasources")
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
            "type": "USER",
            "config": {
              "url": "jdbc:sqlite:file::memory:?cache=shared",
              "dbKind": "sqlite",
              "password": "",
              "username": ""
            }
          }
        """)
      .put(Resources.BASE_PATH + "/datasources/{datasourceName}", "sqlite_test")
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
             "type": "USER",
             "config": {
               "url": "${MYSQL_URL}",
               "dbKind": "mysql",
               "password": "${MYSQL_PASSWORD}",
               "username": "${MYSQL_USERNAME}"
             }
           }
        """)
      .post(Resources.BASE_PATH + "/datasources")
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .delete(Resources.BASE_PATH + "/datasources/{datasourceName}", "mysql_test3")
      .then()
      .statusCode(204);
  }

}
