package tech.wetech.flexmodel.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import tech.wetech.flexmodel.MySQLTestResource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(MySQLTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecordResourceTest {

  @Test
  void testFindPagingRecords() {
    given()
      .when()
      .param("current", "1")
      .param("pageSize", "20")
      .param("deep", "true")
      .get("/api/datasources/{datasourceName}/models/{modelName}/records", "system", "Classes")
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
              "gender": "男",
              "age": 11,
              "classId": 2,
              "studentDetail": {
                "description": "张三丰的描述"
              },
              "courses": [
                 {
                   "courseNo":"YuWen",
                   "courseName":"语文"
                 },
                 {
                   "courseNo":"Eng",
                   "courseName":"英语"
                 }
              ]
            }
        """)
      .post("/api/datasources/{datasourceName}/models/{modelName}/records", "system", "Student")
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
              "gender": "男",
              "age": 11,
              "classId": 2,
              "studentDetail": {
                "description": "张三丰的描述"
              },
              "courses": [
                 {
                   "courseNo":"YuWen",
                   "courseName":"语文"
                 },
                 {
                   "courseNo":"Eng",
                   "courseName":"英语"
                 }
              ]
            }
        """)
      .put("/api/datasources/{datasourceName}/models/{modelName}/records/{recordId}", "system", "Student", 100000)
      .then()
      .statusCode(200);
    // todo 级联更新
  }

  @Test
  @Order(3)
  void testFindOneRecord() {
    given()
      .when()
      .param("current", "1")
      .param("pageSize", "20")
      .param("deep", "true")
      .get("/api/datasources/{datasourceName}/models/{modelName}/records/{recordId}", "system", "Student", 100000)
      .then()
      .statusCode(200)
      .body(
        "studentName", equalTo("张三丰"),
        "studentDetail.description", equalTo("张三丰的描述"),
        "courses.size()", equalTo(2)
      );
  }

  @Test
  @Order(4)
  void testDeleteRecord() {
    given()
      .when()
      .delete("/api/datasources/{datasourceName}/models/{modelName}/records/{recordId}", "system", "Student", 100000)
      .then()
      .statusCode(204);
  }

}
