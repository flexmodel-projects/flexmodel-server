package tech.wetech.flexmodel.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.interfaces.rest.Resources;

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
      .get(Resources.ROOT_PATH + "/datasources/{datasourceName}/models", "system")
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models", "mysql_test")
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .delete(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}", "mysql_test", "testDropModel")
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/fields", "mysql_test", "testCreateFieldStudent")
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
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
      .put(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/fields/{fieldName}",
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models", "mysql_test")
      .then()
      .statusCode(200);
    given()
      .when()
      .delete(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/fields/{fieldName}",
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models",
        "mysql_test"
      )
      .then()
      .statusCode(200);
    given()
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/indexes",
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models",
        "mysql_test"
      )
      .then()
      .statusCode(200);
    given()
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
      .put(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/indexes/{indexName}",
        "mysql_test",
        "testModifyIndexStudent",
        "IDX_gender_wedsd"
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
      .post(Resources.ROOT_PATH + "/datasources/{datasourceName}/models",
        "mysql_test"
      )
      .then()
      .statusCode(200);
    given()
      .when()
      .contentType(ContentType.JSON)
      .delete(Resources.ROOT_PATH + "/datasources/{datasourceName}/models/{modelName}/indexes/{indexName}",
        "mysql_test",
        "testModifyIndexStudent",
        "IDX_gender"
      )
      .then()
      .statusCode(204);
  }
}
