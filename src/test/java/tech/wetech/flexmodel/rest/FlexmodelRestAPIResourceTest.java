package tech.wetech.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.SQLiteTestResource;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class FlexmodelRestAPIResourceTest {

  @Test
  void testGET() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
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
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
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
  void testGenerate() {

    String script = """
      model testGenerateStudent {
        id : Long @id @default(autoIncrement()),
        student_name? : String @length("255"),
        gender? : UserGender,
        interest? : user_interest[],
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
      .post(Resources.ROOT_PATH + "/datasources/system/import")
      .then()
      .statusCode(204);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .header("X-Tenant-Id", "system")
      .when()
      .contentType(ContentType.JSON)
      .body("""
         {"datasourceName":"system","modelName":"testGenerateStudent","apiFolder":"testGenerateStudent","idFieldOfPath":"id","generateAPIs":["list","view","create","update","delete","pagination"]}
        """)
      .post(Resources.ROOT_PATH + "/apis/generate")
      .then()
      .statusCode(204);

  }

}
