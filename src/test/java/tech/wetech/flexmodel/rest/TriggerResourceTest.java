package dev.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ScheduleResource 集成测试
 *
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class TriggerResourceTest {

  private static final String BASE_PATH = "/api/v1/projects/dev_test/triggers";

  // 测试数据中的触发器ID
  private static final String INTERVAL_TRIGGER_ID = "bf492f37-1f01-4eb8-b76d-d319299b4d8e";
  private static final String CRON_TRIGGER_ID = "d8c60d2a-19d8-4c3c-b370-96318733858f";
  private static final String DAILY_TRIGGER_ID = "78505887-128a-4b24-a637-42ea8836a69b";
  private static final String EVENT_AFTER_TRIGGER_ID = "f351b8d9-a450-4f2c-8fff-cf862d690352";
  private static final String TEST_JOB_ID = "5c41f37a-87a9-47af-bdba-44d0c27eda89";

  @BeforeEach
  void setUp() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    String triggerJson = """
      {
          "name": "测试间隔触发",
          "description": "测试描述",
          "type": "SCHEDULED",
          "config": {
              "type": "interval",
              "interval": 5,
              "intervalUnit": "minute",
              "repeatCount": 10
          },
          "jobId": "%s",
          "jobType": "FLOW",
          "state": true
      }
      """.formatted(TEST_JOB_ID);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType(ContentType.JSON)
      .body(triggerJson)
      .when()
      .post(BASE_PATH)
      .then()
      .statusCode(200);
  }

  /**
   * 测试获取触发器列表 - 无过滤条件
   */
  @Test
  void testFindPageWithoutFilter() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(BASE_PATH)
      .then()
      .statusCode(200)
      .body("total", greaterThanOrEqualTo(5))
      .body("list", hasSize(greaterThanOrEqualTo(5)))
      .body("list[0].id", notNullValue())
      .body("list[0].name", notNullValue())
      .body("list[0].type", notNullValue());
  }

  /**
   * 测试获取触发器列表 - 按名称过滤
   */
  @Test
  void testFindPageWithNameFilter() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .queryParam("name", "定时触发-间隔触发")
      .when()
      .get(BASE_PATH)
      .then()
      .statusCode(200)
      .body("total", equalTo(1))
      .body("list", hasSize(1))
      .body("list[0].name", equalTo("定时触发-间隔触发"));
  }

  /**
   * 测试获取触发器列表 - 分页
   */
  @Test
  void testFindPageWithPagination() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .queryParam("page", 1)
      .queryParam("size", 2)
      .when()
      .get(BASE_PATH)
      .then()
      .statusCode(200)
      .body("list", hasSize(lessThanOrEqualTo(2)))
      .body("total", greaterThanOrEqualTo(5));
  }

  /**
   * 测试创建触发器 - 间隔触发
   */
  @Test
  void testCreateIntervalTrigger() {
    String triggerJson = """
      {
          "name": "测试间隔触发",
          "description": "测试描述",
          "type": "SCHEDULED",
          "config": {
              "type": "interval",
              "interval": 5,
              "intervalUnit": "minute",
              "repeatCount": 10
          },
          "jobId": "%s",
          "jobType": "FLOW",
          "state": true
      }
      """.formatted(TEST_JOB_ID);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType(ContentType.JSON)
      .body(triggerJson)
      .when()
      .post(BASE_PATH)
      .then()
      .statusCode(200)
      .body("id", notNullValue())
      .body("name", equalTo("测试间隔触发"))
      .body("description", equalTo("测试描述"))
      .body("type", equalTo("SCHEDULED"))
      .body("state", equalTo(true))
      .body("jobId", equalTo(TEST_JOB_ID))
      .body("jobType", equalTo("FLOW"))
      .body("config.interval", equalTo(5))
      .body("config.intervalUnit", equalTo("minute"))
      .body("config.repeatCount", equalTo(10));
  }

  /**
   * 测试创建触发器 - Cron表达式触发
   */
  @Test
  void testCreateCronTrigger() {
    String triggerJson = """
      {
          "name": "测试Cron触发",
          "description": "测试Cron描述",
          "type": "SCHEDULED",
          "config": {
              "type": "cron",
              "cronExpression": "0 0 8 * * ?"
          },
          "jobId": "%s",
          "jobType": "FLOW",
          "state": true
      }
      """.formatted(TEST_JOB_ID);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType(ContentType.JSON)
      .body(triggerJson)
      .when()
      .post(BASE_PATH)
      .then()
      .statusCode(200)
      .body("id", notNullValue())
      .body("name", equalTo("测试Cron触发"))
      .body("type", equalTo("SCHEDULED"))
      .body("config.cronExpression", equalTo("0 0 8 * * ?"));
  }

  /**
   * 测试创建触发器 - 事件触发
   */
  @Test
  void testCreateEventTrigger() {
    String triggerJson = """
      {
          "name": "测试事件触发",
          "description": "测试事件描述",
          "type": "EVENT",
          "config": {
              "type": "event",
              "datasourceName": "dev_test",
              "modelName": "Student",
              "mutationTypes": ["create", "update"],
              "triggerTiming": "after"
          },
          "jobId": "%s",
          "jobType": "FLOW",
          "state": true
      }
      """.formatted(TEST_JOB_ID);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType(ContentType.JSON)
      .body(triggerJson)
      .when()
      .post(BASE_PATH)
      .then()
      .statusCode(200)
      .body("id", notNullValue())
      .body("name", equalTo("测试事件触发"))
      .body("type", equalTo("EVENT"))
      .body("config.datasourceName", equalTo("dev_test"))
      .body("config.modelName", equalTo("Student"))
      .body("config.triggerTiming", equalTo("after"));
  }

  /**
   * 测试更新触发器
   */
  @Test
  void testUpdateTrigger() {
    String updateJson = """
      {
          "id": "%s",
          "name": "更新后的触发器名称",
          "description": "更新后的描述",
          "type": "SCHEDULED",
          "config": {
              "type": "interval",
              "interval": 10,
              "intervalUnit": "minute",
              "repeatCount": 20
          },
          "jobId": "%s",
          "jobType": "FLOW",
          "state": false
      }
      """.formatted(INTERVAL_TRIGGER_ID, TEST_JOB_ID);

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType(ContentType.JSON)
      .body(updateJson)
      .when()
      .put(BASE_PATH + "/" + INTERVAL_TRIGGER_ID)
      .then()
      .statusCode(200)
      .body("id", equalTo(INTERVAL_TRIGGER_ID))
      .body("name", equalTo("更新后的触发器名称"))
      .body("description", equalTo("更新后的描述"))
      .body("state", equalTo(false))
      .body("config.interval", equalTo(10))
      .body("config.repeatCount", equalTo(20));
  }

  /**
   * 测试部分更新触发器 - 只更新状态
   */
  @Test
  void testPatchTrigger() {
    String patchJson = """
      {
          "state": false
      }
      """;

    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType(ContentType.JSON)
      .body(patchJson)
      .when()
      .patch(BASE_PATH + "/" + CRON_TRIGGER_ID)
      .then()
      .statusCode(200)
      .body("id", equalTo(CRON_TRIGGER_ID))
      .body("name", equalTo("定时触发-Cron表达式")) // 原有名称保持不变
      .body("state", equalTo(false)); // 只有状态被更新
  }

  /**
   * 测试立即执行触发器
   */
  @Test
  void testExecuteNow() {
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .post(BASE_PATH + "/" + INTERVAL_TRIGGER_ID + "/execute")
      .then()
      .statusCode(400);
  }

  /**
   * 测试删除触发器
   */
  @Test
  void testDeleteTrigger() {
    // 先创建一个触发器用于删除测试
    String triggerJson = """
      {
          "name": "待删除的触发器",
          "description": "用于删除测试",
          "type": "SCHEDULED",
          "config": {
              "type": "interval",
              "interval": 1,
              "intervalUnit": "minute",
              "repeatCount": 1
          },
          "jobId": "%s",
          "jobType": "FLOW",
          "state": true
      }
      """.formatted(TEST_JOB_ID);

    String createdTriggerId = given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .contentType(ContentType.JSON)
      .body(triggerJson)
      .when()
      .post(BASE_PATH)
      .then()
      .statusCode(200)
      .extract()
      .path("id");

    // 删除创建的触发器
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .delete(BASE_PATH + "/" + createdTriggerId)
      .then()
      .statusCode(204);

    // 验证触发器已被删除
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(BASE_PATH + "/" + createdTriggerId)
      .then()
      .statusCode(204);
  }

  /**
   * 测试验证不同类型的触发器配置
   */
  @Test
  void testValidateDifferentTriggerTypes() {
    // 验证间隔触发配置
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(BASE_PATH + "/" + INTERVAL_TRIGGER_ID)
      .then()
      .statusCode(200)
      .body("type", equalTo("SCHEDULED"))
      .body("config.interval", notNullValue())
      .body("config.intervalUnit", notNullValue())
      .body("config.repeatCount", notNullValue());

    // 验证Cron触发配置
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(BASE_PATH + "/" + CRON_TRIGGER_ID)
      .then()
      .statusCode(200)
      .body("type", equalTo("SCHEDULED"))
      .body("config.cronExpression", equalTo("0 0 * * * ? *"));

    // 验证事件触发配置
    given()
      .header("Authorization", TestTokenHelper.getAuthorizationHeader())
      .when()
      .get(BASE_PATH + "/" + EVENT_AFTER_TRIGGER_ID)
      .then()
      .statusCode(200)
      .body("type", equalTo("EVENT"))
      .body("config.datasourceName", equalTo("dev_test"))
      .body("config.modelName", equalTo("Classes"))
      .body("config.mutationTypes", notNullValue())
      .body("config.triggerTiming", equalTo("after"));
  }

}
