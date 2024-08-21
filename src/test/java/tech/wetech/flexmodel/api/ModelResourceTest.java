package tech.wetech.flexmodel.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * @author cjbi
 */
@QuarkusTest
class ModelResourceTest {

  @Test
  void testFindModels() {
    given()
      .when()
      .get("/api/datasources/{datasourceName}/models", "system")
      .then()
      .statusCode(200)
      .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  void testCreateModel() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testCreateModelStudent",
          "fields": [
            {
              "name": "id",
              "type": "id",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post("/api/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
  }

  @Test
  void testDropModel() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testDropModelStudent",
          "fields": [
            {
              "name": "id",
              "type": "id",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post("/api/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .delete("/api/datasources/{datasourceName}/models/{modelName}", "mysql_test", "testDropModel")
      .then()
      .statusCode(204);
  }

  @Test
  void testCreateField() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testCreateFieldStudent",
          "fields": [
            {
              "name": "id",
              "type": "id",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post("/api/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "studentFirstName",
          "type": "string",
          "unique": false,
          "nullable": true,
          "validators": [],
          "length": 255
        }
        """)
      .post("/api/datasources/{datasourceName}/models/{modelName}/fields", "mysql_test", "testCreateFieldStudent")
      .then()
      .statusCode(200);
  }

  @Test
  void testModifyField() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testModifyFieldStudent",
          "fields": [
            {
              "name": "id",
              "type": "id",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "string",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "string",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "int",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "int",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post("/api/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "studentName",
          "type": "string",
          "unique": true,
          "nullable": false,
          "validators": [],
          "length": 500
        }
        """)
      .put("/api/datasources/{datasourceName}/models/{modelName}/fields/{fieldName}",
        "mysql_test",
        "testModifyFieldStudent",
        "studentName")
      .then()
      .statusCode(200);
  }

  @Test
  void testDropField() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testDropFieldStudent",
          "fields": [
            {
              "name": "id",
              "type": "id",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ],
          "indexes": []
        }
        """)
      .post("/api/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .delete("/api/datasources/{datasourceName}/models/{modelName}/fields/{fieldName}",
        "mysql_test",
        "testDropFieldStudent",
        "studentName")
      .then()
      .statusCode(204);
  }

  @Test
  void testCreateIndex() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testCreateIndexStudent",
          "fields": [
            {
              "name": "id",
              "type": "id",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post("/api/datasources/{datasourceName}/models",
        "mysql_test"
      )
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "IDX_gender",
          "modelName": "testCreateIndexStudent",
          "fields": [
            {
              "fieldName": "gender",
              "direction": "ASC"
            }
          ],
          "unique": false
        }
        """)
      .post("/api/datasources/{datasourceName}/models/{modelName}/indexes",
        "mysql_test",
        "testCreateIndexStudent"
      )
      .then()
      .statusCode(200);
  }

  @Test
  void testModifyIndex() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testModifyIndexStudent",
          "fields": [
            {
              "name": "id",
              "type": "id",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ],
          "indexes": [{
          "name": "IDX_gender",
          "modelName": "testModifyIndexStudent",
          "fields": [
            {
              "fieldName": "gender",
              "direction": "ASC"
            }
          ],
          "unique": false
        }]
        }
        """)
      .post("/api/datasources/{datasourceName}/models",
        "mysql_test"
      )
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "IDX_gender",
          "modelName": "testModifyIndexStudent",
          "fields": [
            {
              "fieldName": "gender",
              "direction": "DESC"
            }
          ],
          "unique": true
        }
        """)
      .put("/api/datasources/{datasourceName}/models/{modelName}/indexes/{indexName}",
        "mysql_test",
        "testModifyIndexStudent",
        "IDX_gender"
      )
      .then()
      .statusCode(200);
  }

  @Test
  void testDropIndex() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testDropIndexStudent",
          "fields": [
            {
              "name": "id",
              "type": "id",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "string",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ],
          "indexes": [{
          "name": "IDX_gender",
          "modelName": "testDropIndexStudent",
          "fields": [
            {
              "fieldName": "gender",
              "direction": "ASC"
            }
          ],
          "unique": false
        }]
        }
        """)
      .post("/api/datasources/{datasourceName}/models",
        "mysql_test"
      )
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .delete("/api/datasources/{datasourceName}/models/{modelName}/indexes/{indexName}",
        "mysql_test",
        "testModifyIndexStudent",
        "IDX_gender"
      )
      .then()
      .statusCode(204);
  }
}
