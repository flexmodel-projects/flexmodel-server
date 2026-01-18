package dev.flexmodel.rest;

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
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models", "system")
      .then()
      .statusCode(200)
      .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  void testCreateModel() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testCreateModelStudent",
          "fields": [
            {
              "name": "id",
              "type": "Long",
              "identity": true,
              "unique": false,
              "nullable": true
            },
            {
              "name": "studentName",
              "type": "String",
              "unique": false,
              "nullable": true,
              "length": 255
            },
            {
              "name": "gender",
              "type": "String",
              "unique": false,
              "nullable": true,
              "length": 255
            },
            {
              "name": "age",
              "type": "Int",
              "unique": false,
              "nullable": true
            },
            {
              "name": "classId",
              "type": "Int",
              "unique": false,
              "nullable": true
            }
          ]
        }
        """)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models", "sqlite_test")
      .then()
      .statusCode(200);
  }

  @Test
  void testDropModel() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testDropModelStudent",
          "fields": [
            {
              "name": "id",
              "type": "Long",
              "identity": true,
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "studentName",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models", "sqlite_test")
      .then()
      .statusCode(200);
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .delete(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models/{modelName}", "sqlite_test", "testDropModel")
      .then()
      .statusCode(204);
  }

  @Test
  void testCreateField() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testCreateFieldStudent",
          "fields": [
            {
              "name": "id",
              "type": "Long",
              "identity": true,
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "studentName",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models", "sqlite_test")
      .then()
      .statusCode(200);
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "studentFirstName",
          "type": "String",
          "unique": false,
          "nullable": true,
          "validators": [],
          "length": 255
        }
        """)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models/{modelName}/fields", "sqlite_test", "testCreateFieldStudent")
      .then()
      .statusCode(200);
  }

  @Test
  void testModifyField() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testModifyFieldStudent",
          "fields": [
            {
              "name": "id",
              "type": "Long",
              "identity": true,
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true
            },
            {
              "name": "studentName",
              "type": "String",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "String",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "Int",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "Int",
              "modelName": "testModifyFieldStudent",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models", "sqlite_test")
      .then()
      .statusCode(200);
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "studentName",
          "type": "String",
          "unique": true,
          "nullable": false,
          "validators": [],
          "length": 500
        }
        """)
      .put(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models/{modelName}/fields/{fieldName}",
        "sqlite_test",
        "testModifyFieldStudent",
        "studentName")
      .then()
      .statusCode(200);
  }

  @Test
  void testDropField() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testDropFieldStudent",
          "fields": [
            {
              "name": "id",
              "type": "Long",
              "identity": true,
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "studentName",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ],
          "indexes": []
        }
        """)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models", "sqlite_test")
      .then()
      .statusCode(200);
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .delete(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models/{modelName}/fields/{fieldName}",
        "sqlite_test",
        "testDropFieldStudent",
        "studentName")
      .then()
      .statusCode(204);
  }

  @Test
  void testCreateIndex() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testCreateIndexStudent",
          "fields": [
            {
              "name": "id",
              "type": "Long",
              "identity": true,
              "modelName": "Student",
              "unique": false,
              "nullable": true
            },
            {
              "name": "studentName",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ]
        }
        """)
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models",
        "sqlite_test"
      )
      .then()
      .statusCode(200);
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "IDX_gender_sdsws",
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
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models/{modelName}/indexes",
        "sqlite_test",
        "testCreateIndexStudent"
      )
      .then()
      .statusCode(200);
  }

  @Test
  void testModifyIndex() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testModifyIndexStudent",
          "fields": [
            {
              "name": "id",
              "type": "Long",
              "identity": true,
              "modelName": "Student",
              "unique": false,
              "nullable": true
            },
            {
              "name": "studentName",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            }
          ],
          "indexes": [{
          "name": "IDX_gender_wedsd",
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
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models",
        "sqlite_test"
      )
      .then()
      .statusCode(200);
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "IDX_gender_wedsd",
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
      .put(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models/{modelName}/indexes/{indexName}",
        "sqlite_test",
        "testModifyIndexStudent",
        "IDX_gender_wedsd"
      )
      .then()
      .statusCode(200);
  }

  @Test
  void testDropIndex() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .body("""
        {
          "type": "entity",
          "name": "testDropIndexStudent",
          "fields": [
            {
              "name": "id",
              "type": "Long",
              "identity": true,
              "modelName": "Student",
              "unique": false,
              "nullable": true
            },
            {
              "name": "studentName",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "gender",
              "type": "String",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": [],
              "length": 255
            },
            {
              "name": "age",
              "type": "Int",
              "modelName": "Student",
              "unique": false,
              "nullable": true,
              "validators": []
            },
            {
              "name": "classId",
              "type": "Int",
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
      .post(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models",
        "sqlite_test"
      )
      .then()
      .statusCode(200);
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .contentType(ContentType.JSON)
      .delete(Resources.ROOT_PATH + "/projects/dev_test/datasources/{datasourceName}/models/{modelName}/indexes/{indexName}",
        "sqlite_test",
        "testModifyIndexStudent",
        "IDX_gender"
      )
      .then()
      .statusCode(204);
  }
}
