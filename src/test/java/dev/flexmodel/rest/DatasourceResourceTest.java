package dev.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
class DatasourceResourceTest {

  /**
   * 获取测试用的token
   */
  @Test
  void testValidateConnection() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
             "name": "mysql_test",
             "type": "USER",
             "config": {
               "url": "${SQLITE_URL}",
               "dbKind": "mysql",
               "password": "${SQLITE_PASSWORD}",
               "username": "${SQLITE_USERNAME}"
             }
           }
        """)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/validate")
      .then()
      .statusCode(200)
      .body("success", is(true));
  }

  @Test
  void testImportModels() {
    String script = """
      model testImportModelsStudent {
        id : Long @id @default(autoIncrement()),
        student_name? : String @length("255"),
        age? : Int,
        class_id? : Long,
        @index(name: "IDX_STUDENT_NAME",unique: "false", fields: [student_name]),
        @index(name:"IDX_CLASS_ID", unique: "false", fields: [class_id]),
        @comment("学生")
      }
      """;
    Map<String,Object> body = Map.of("type", "IDL", "script", script);
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body(body)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/system/import")
      .then()
      .statusCode(204);
  }

  @Test
  void testFindAll() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(Resources.ROOT_PATH + "/projects/dev_test/datasources")
      .then()
      .statusCode(200)
      .body("size()", greaterThanOrEqualTo(1));

  }

  @Test
  void testCreateDatasource() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
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
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources")
      .then()
      .statusCode(200);
  }

  @Test
  void testUpdateDatasource() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
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
      .put(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}", "sqlite_test")
      .then()
      .statusCode(200);
  }

  @Test
  void testDeleteDatasource() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
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
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources")
      .then()
      .statusCode(200);
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .delete(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}", "mysql_test3")
      .then()
      .statusCode(204);
  }

}
