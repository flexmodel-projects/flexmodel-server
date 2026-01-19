package dev.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class FlexmodelRestAPIResourceTest {

  @BeforeAll
  public static void beforeAll() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @Test
  void testGET() {
    // list
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get("/api/dev_test/Classes/list")
      .then()
      .statusCode(200)
      .body("size()", greaterThanOrEqualTo(1))
      .body("data.dev_test_list_Classes[0].classCode", notNullValue());
    // view
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get("/api/dev_test/Classes/1")
      .then()
      .statusCode(200)
      .body("data.dev_test_find_one_Classes.classCode", notNullValue());
  }

  @Test
  void testPOST() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "id": 999,
            "classCode": "C_004",
            "className": "三年级2班"
          }
        """)
      .post("/api/dev_test/Classes")
      .then()
      .statusCode(200)
      .body("data", notNullValue());
  }

  @Test
  void testPUT() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "id": 1,
            "classCode": "C_10",
            "className": "五年级3班"
          }
        """)
      .put("/api/dev_test/Classes/1")
      .then()
      .statusCode(200)
      .body("data", notNullValue());
  }

  @Test
  void testDELETE() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .delete("/api/dev_test/Classes/1")
      .then()
      .statusCode(200)
      .body("data", notNullValue());
  }

  @Test
  void testGraphQL() {

    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "query": "query { dev_test_list_Classes { classCode className } }"
          }
        """)
      .post("/api/dev_test/graphql")
      .then()
      .statusCode(200)
      .body("data.dev_test_list_Classes", notNullValue());
  }

  @Test
  @SuppressWarnings("all")
  void testGenerate() throws InterruptedException {

    String script = """
      model testGenerateStudent {
        id : Long @id @default(autoIncrement()),
        student_name? : String @length("255"),
        gender? : String,
        interest? : String,
        age? : Int,
        class_id? : Long,
        @index(name: "IDX_STUDENT_NAME",unique: "false", fields: [student_name]),
        @index(name:"IDX_CLASS_ID", unique: "false", fields: [class_id]),
        @comment("学生")
      }
      """;
    Map<String, Object> body = Map.of("type", "IDL", "script", script);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body(body)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/dev_test/import")
      .then()
      .statusCode(204);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
         {
            "datasourceName":"dev_test",
            "modelName":"testGenerateStudent",
            "apiFolder":"testGenerateStudent",
            "idFieldOfPath":"id",
            "generateAPIs":["list", "pagination", "view", "create", "update", "delete"]
         }
        """)
      .post(Resources.ROOT_PATH + "/projects/dev_test/apis/generate")
      .then()
      .statusCode(204);

    Thread.sleep(1000);

    // create
    Map createRes = given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "student_name": "张三",
            "gender": "男",
            "interest": "读书,游戏,逛街",
            "age": 18
          }
        """)
      .post("/api/dev_test/testGenerateStudent")
      .then()
      .statusCode(200)
      .body(
        "data.dev_test_create_testGenerateStudent.student_name", equalTo("张三"),
        "data.dev_test_create_testGenerateStudent.gender", equalTo("男"),
        "data.dev_test_create_testGenerateStudent.age", equalTo(18)
      ).extract().as(Map.class);

    Map data = (Map) createRes.get("data");
    Map<String, Object> studentMap = (Map<String, Object>) data.get("dev_test_create_testGenerateStudent");

    // list
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .get("/api/dev_test/testGenerateStudent/list")
      .then()
      .statusCode(200)
      .body("data.list.size()", greaterThanOrEqualTo(1));

    // page
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .param("page", 1)
      .param("size", 1)
      .when()
      .contentType(ContentType.JSON)
      .get("/api/dev_test/testGenerateStudent/page")
      .then()
      .statusCode(200)
      .body(
        "data.list.size()", equalTo(1),
        "data.total", greaterThanOrEqualTo(1)
      );

    // view
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .get("/api/dev_test/testGenerateStudent/{id}", studentMap.get("id"))
      .then()
      .statusCode(200)
      .body("data.dev_test_find_one_testGenerateStudent.id", equalTo(studentMap.get("id")));

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "student_name": "张三丰",
            "gender": "男",
            "interest": "读书,打太极",
            "age": 300
          }
        """)
      .put("/api/dev_test/testGenerateStudent/{id}", studentMap.get("id"))
      .then()
      .statusCode(200)
      .body(
        "data.dev_test_update_testGenerateStudent.affected_rows", equalTo(1)
      );

    // delete
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .delete("/api/dev_test/testGenerateStudent/{id}", studentMap.get("id"))
      .then()
      .statusCode(200)
      .body("data.dev_test_delete_testGenerateStudent.affected_rows", equalTo(1));

  }

}
