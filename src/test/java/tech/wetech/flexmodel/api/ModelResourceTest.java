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
      .get(Resources.BASE_PATH + "/datasources/{datasourceName}/models", "system")
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
          "type": "ENTITY",
          "name": "testCreateModelStudent",
          "fields": [
            {
              "name": "id",
              "type": "ID",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models", "mysql_test")
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
          "type": "ENTITY",
          "name": "testDropModelStudent",
          "fields": [
            {
              "name": "id",
              "type": "ID",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .delete(Resources.BASE_PATH + "/datasources/{datasourceName}/models/{modelName}", "mysql_test", "testDropModel")
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
          "type": "ENTITY",
          "name": "testCreateFieldStudent",
          "fields": [
            {
              "name": "id",
              "type": "ID",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "studentFirstName",
          "type": "STRING",
          "unique": false,
          "nullable": true,
          "validators": [],
          "length": 255
        }
        """)
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models/{modelName}/fields", "mysql_test", "testCreateFieldStudent")
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
          "type": "ENTITY",
          "name": "testModifyFieldStudent",
          "fields": [
            {
              "name": "id",
              "type": "ID",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "STRING",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "STRING",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "INT",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "INT",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "studentName",
          "type": "STRING",
          "unique": true,
          "nullable": false,
          "validators": [],
          "length": 500
        }
        """)
      .put(Resources.BASE_PATH + "/datasources/{datasourceName}/models/{modelName}/fields/{fieldName}",
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
          "type": "ENTITY",
          "name": "testDropFieldStudent",
          "fields": [
            {
              "name": "id",
              "type": "ID",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ],
          "indexes": []
        }
        """)
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .delete(Resources.BASE_PATH + "/datasources/{datasourceName}/models/{modelName}/fields/{fieldName}",
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
          "type": "ENTITY",
          "name": "testCreateIndexStudent",
          "fields": [
            {
              "name": "id",
              "type": "ID",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models",
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
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models/{modelName}/indexes",
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
          "type": "ENTITY",
          "name": "testModifyIndexStudent",
          "fields": [
            {
              "name": "id",
              "type": "ID",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "INT",
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
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models",
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
      .put(Resources.BASE_PATH + "/datasources/{datasourceName}/models/{modelName}/indexes/{indexName}",
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
          "type": "ENTITY",
          "name": "testDropIndexStudent",
          "fields": [
            {
              "name": "id",
              "type": "ID",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "generatedValue": "BIGINT_NOT_GENERATED"
            },
            {
              "name": "studentName",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "STRING",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "INT",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "INT",
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
      .post(Resources.BASE_PATH + "/datasources/{datasourceName}/models",
        "mysql_test"
      )
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .delete(Resources.BASE_PATH + "/datasources/{datasourceName}/models/{modelName}/indexes/{indexName}",
        "mysql_test",
        "testModifyIndexStudent",
        "IDX_gender"
      )
      .then()
      .statusCode(204);
  }
}
