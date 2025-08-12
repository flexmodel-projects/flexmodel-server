package tech.wetech.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import tech.wetech.flexmodel.SQLiteTestResource;
import tech.wetech.flexmodel.interfaces.rest.Resources;

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

  @Test
  void testFindPagingRecords() {
    given()
      .when()
      .param("current", "1")
      .param("pageSize", "20")
      .param("nestedQuery", "true")
      .get(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/records", "system", "Classes")
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/records", "system", "Student")
      .then()
      .statusCode(200);
  }

  @Test
  @Order(2)
  void testUpdateRecord() {
    given()
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
      .put(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/records/{recordId}", "system", "Student", 100000)
      .then()
      .statusCode(200);
    // todo 级联更新
  }

  @Test
  @Order(3)
  void testFindOneRecord() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    given()
      .when()
      .param("current", "1")
      .param("pageSize", "20")
      .param("nestedQuery", "true")
      .get(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/records/{recordId}", "system", "Student", 100000)
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
      .when()
      .delete(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/records/{recordId}", "system", "Student", 100000)
      .then()
      .statusCode(204);
  }

}
