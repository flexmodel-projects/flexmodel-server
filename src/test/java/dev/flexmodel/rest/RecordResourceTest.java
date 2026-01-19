package dev.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import dev.flexmodel.SQLiteTestResource;
import dev.flexmodel.interfaces.rest.jwt.JwtUtil;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecordResourceTest {

  /**
   * 获取测试用的token
   */
  private String getTestToken() {
    return JwtUtil.sign("admin", Duration.ofMinutes(5));
  }

  @Test
  void testFindPagingRecords() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .param("current", "1")
      .param("pageSize", "20")
      .param("nestedQuery", "true")
      .get(Resources.ROOT_PATH + "/projects/{projectId}/datasources/{datasourceName}/models/{modelName}/records", "dev_test", "dev_test", "Classes")
      .then()
      .statusCode(200)
      .body(
        "size()", greaterThanOrEqualTo(1),
        "list[0].students.size()", greaterThanOrEqualTo(1)
      );
  }

  @Test
  @Order(1)
  void testCreateRecord() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
            {
              "id": 100000,
              "studentName": "张三丰",
              "gender": "MALE",
              "age": 11,
              "classId": 2,
              "studentDetail": {
                "description": "张三丰的描述"
              }
            }
        """)
      .post(Resources.ROOT_PATH + "/projects/{projectId}/datasources/{datasourceName}/models/{modelName}/records", "dev_test", "dev_test", "Student")
      .then()
      .statusCode(200);
  }

  @Test
  @Order(2)
  void testUpdateRecord() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
            {
              "id": 100000,
              "studentName": "张三丰",
              "gender": "MALE",
              "age": 11,
              "classId": 2,
              "studentDetail": {
                "description": "张三丰的描述"
              }
            }
        """)
      .put(Resources.ROOT_PATH + "/projects/{projectId}/datasources/{datasourceName}/models/{modelName}/records/{recordId}", "dev_test", "dev_test", "Student", 100000)
      .then()
      .statusCode(200);
    // todo 级联更新
  }

  @Test
  @Order(3)
  void testFindOneRecord() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .param("current", "1")
      .param("pageSize", "20")
      .param("nestedQuery", "true")
      .get(Resources.ROOT_PATH + "/projects/{projectId}/datasources/{datasourceName}/models/{modelName}/records/{recordId}", "dev_test", "dev_test", "Student", 100000)
      .then()
      .statusCode(200)
      .body(
        "studentName", equalTo("张三丰"),
        "studentDetail.description", equalTo("张三丰的描述")
      );
  }

  @Test
  @Order(4)
  void testDeleteRecord() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .delete(Resources.ROOT_PATH + "/projects/{projectId}/datasources/{datasourceName}/models/{modelName}/records/{recordId}", "dev_test", "dev_test", "Student", 100000)
      .then()
      .statusCode(204);
  }

}
