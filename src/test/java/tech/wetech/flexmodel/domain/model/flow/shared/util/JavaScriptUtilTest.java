package tech.wetech.flexmodel.domain.model.flow.shared.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JavaScriptUtil测试用例
 *
 * @author cjbi
 */
class JavaScriptUtilTest {

  @AfterEach
  void cleanup() {
    // 每次测试后清理Context
    JavaScriptUtil.cleanup();
  }

  @Test
  void testExecuteSimpleExpression() throws Exception {
    // 测试简单算术表达式
    Object result = JavaScriptUtil.execute("1 + 1", new HashMap<>());
    assertEquals(2, result);
  }

  @Test
  void testExecuteWithVariable() throws Exception {
    // 测试使用变量的表达式
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("x", 10);
    dataMap.put("y", 20);

    Object result = JavaScriptUtil.execute("x + y", dataMap);
    assertEquals(30, result);
  }

  @Test
  void testExecuteStringOperation() throws Exception {
    // 测试字符串操作
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("firstName", "John");
    dataMap.put("lastName", "Doe");

    Object result = JavaScriptUtil.execute("firstName + ' ' + lastName", dataMap);
    assertEquals("John Doe", result);
  }

  @Test
  void testExecuteBooleanExpression() throws Exception {
    // 测试布尔表达式
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("age", 25);

    Object result = JavaScriptUtil.execute("age > 18", dataMap);
    assertEquals(true, result);
  }

  @Test
  void testExecuteComplexExpression() throws Exception {
    // 测试复杂表达式
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("price", 100);
    dataMap.put("discount", 0.2);

    Object result = JavaScriptUtil.execute("price * (1 - discount)", dataMap);
    assertEquals(80.0, result);
  }

  @Test
  void testExecuteConditionalExpression() throws Exception {
    // 测试三元表达式
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("score", 85);

    Object result = JavaScriptUtil.execute("score >= 60 ? 'pass' : 'fail'", dataMap);
    assertEquals("pass", result);
  }

  @Test
  void testExecuteArrayOperation() throws Exception {
    // 测试数组操作
    Object result = JavaScriptUtil.execute("[1, 2, 3, 4, 5].length", new HashMap<>());
    assertEquals(5, result);
  }

  @Test
  void testExecuteArrayWithVariable() throws Exception {
    // 测试带变量的数组操作
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("numbers", new int[]{1, 2, 3, 4, 5});

    Object result = JavaScriptUtil.execute("numbers.length", dataMap);
    assertEquals(5, result);
  }

  @Test
  void testExecuteMathFunction() throws Exception {
    // 测试Math函数
    Object result = JavaScriptUtil.execute("Math.max(10, 20, 30)", new HashMap<>());
    assertEquals(30, result);
  }

  @Test
  void testExecuteStringMethod() throws Exception {
    // 测试字符串方法
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("text", "hello world");

    Object result = JavaScriptUtil.execute("text.toUpperCase()", dataMap);
    assertEquals("HELLO WORLD", result);
  }

  @Test
  void testExecuteEmptyExpression() throws Exception {
    // 测试空表达式
    Object result = JavaScriptUtil.execute("", new HashMap<>());
    assertNull(result);
  }

  @Test
  void testExecuteNullExpression() throws Exception {
    // 测试null表达式
    Object result = JavaScriptUtil.execute(null, new HashMap<>());
    assertNull(result);
  }

  @Test
  void testExecuteWithMissingVariable() {
    // 测试缺失变量（应抛出异常）
    Map<String, Object> dataMap = new HashMap<>();

    assertThrows(ProcessException.class, () -> {
      JavaScriptUtil.execute("undefinedVariable + 1", dataMap);
    });
  }

  @Test
  void testexecuteSimple() throws Exception {
    // 测试简单脚本
    String script = """
        var a = 10;
        var b = 20;
        a + b;
      """;

    Object result = JavaScriptUtil.execute(script, new HashMap<>());
    assertEquals(30, result);
  }

  @Test
  void testexecuteWithFunction() throws Exception {
    // 测试包含函数的脚本
    String script = """
        function add(x, y) {
          return x + y;
        }
        add(15, 25);
      """;

    Object result = JavaScriptUtil.execute(script, new HashMap<>());
    assertEquals(40, result);
  }

  @Test
  void testexecuteWithLoop() throws Exception {
    // 测试包含循环的脚本
    String script = """
        var sum = 0;
        for (var i = 1; i <= 10; i++) {
          sum += i;
        }
        sum;
      """;

    Object result = JavaScriptUtil.execute(script, new HashMap<>());
    assertEquals(55, result);
  }

  @Test
  void testexecuteWithVariable() throws Exception {
    // 测试脚本中使用外部变量
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("base", 100);

    String script = """
        var multiplier = 2;
        base * multiplier;
      """;

    Object result = JavaScriptUtil.execute(script, dataMap);
    assertEquals(200, result);
  }

  @Test
  void testexecuteWithArrayProcessing() throws Exception {
    // 测试数组处理脚本
    String script = """
        var numbers = [1, 2, 3, 4, 5];
        var sum = 0;
        for (var i = 0; i < numbers.length; i++) {
          sum += numbers[i];
        }
        sum;
      """;

    Object result = JavaScriptUtil.execute(script, new HashMap<>());
    assertEquals(15, result);
  }

  @Test
  void testexecuteWithConditional() throws Exception {
    // 测试条件判断脚本
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("temperature", 25);

    String script = """
        var weather;
        if (temperature > 30) {
          weather = 'hot';
        } else if (temperature > 20) {
          weather = 'warm';
        } else {
          weather = 'cold';
        }
        weather;
      """;

    Object result = JavaScriptUtil.execute(script, dataMap);
    assertEquals("warm", result);
  }

  @Test
  void testexecuteWithObjectOperation() throws Exception {
    // 测试对象操作脚本
    String script = """
        var person = {
          name: 'Alice',
          age: 30
        };
        person.name + ' is ' + person.age + ' years old';
      """;

    Object result = JavaScriptUtil.execute(script, new HashMap<>());
    assertEquals("Alice is 30 years old", result);
  }

  @Test
  void testexecuteEmpty() throws Exception {
    // 测试空脚本
    Object result = JavaScriptUtil.execute("", new HashMap<>());
    assertNull(result);
  }

  @Test
  void testexecuteNull() throws Exception {
    // 测试null脚本
    Object result = JavaScriptUtil.execute(null, new HashMap<>());
    assertNull(result);
  }

  @Test
  void testExecuteNumberTypes() throws Exception {
    // 测试不同数字类型
    Object intResult = JavaScriptUtil.execute("42", new HashMap<>());
    assertEquals(42, intResult);

    Object doubleResult = JavaScriptUtil.execute("3.14", new HashMap<>());
    assertEquals(3.14, doubleResult);

    Object longResult = JavaScriptUtil.execute("9007199254740991", new HashMap<>());
    assertNotNull(longResult);
  }

  @Test
  void testExecuteNullValue() throws Exception {
    // 测试null值
    Object result = JavaScriptUtil.execute("null", new HashMap<>());
    assertNull(result);
  }

  @Test
  void testExecuteUndefined() throws Exception {
    // 测试undefined值
    Object result = JavaScriptUtil.execute("undefined", new HashMap<>());
    assertNull(result);
  }

  @Test
  void testMultipleExecutions() throws Exception {
    // 测试多次执行
    Map<String, Object> dataMap1 = new HashMap<>();
    dataMap1.put("x", 10);
    Object result1 = JavaScriptUtil.execute("x * 2", dataMap1);
    assertEquals(20, result1);

    Map<String, Object> dataMap2 = new HashMap<>();
    dataMap2.put("y", 5);
    Object result2 = JavaScriptUtil.execute("y * 3", dataMap2);
    assertEquals(15, result2);
  }

  @Test
  void testExecuteWithMapVariable() throws Exception {
    // 测试使用Map类型变量
    Map<String, Object> innerMap = new HashMap<>();
    innerMap.put("key1", "value1");
    innerMap.put("key2", 100);

    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("data", innerMap);

    Object result = JavaScriptUtil.execute("data.key2 * 2", dataMap);
    assertEquals(200, result);
  }

  @Test
  void testexecuteReturnObject() throws Exception {
    // 测试脚本返回对象
    String script = """
        (function() {
          return {
            name: 'Alice',
            age: 30,
            city: 'Beijing'
          };
        })()
      """;

    Object result = JavaScriptUtil.execute(script, new HashMap<>());
    assertNotNull(result);
    assertInstanceOf(Map.class, result);

    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertEquals("Alice", resultMap.get("name"));
    assertEquals(30, resultMap.get("age"));
    assertEquals("Beijing", resultMap.get("city"));
  }

  @Test
  void testexecuteReturnObjectWithVariables() throws Exception {
    // 测试脚本使用变量返回对象
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("userName", "Bob");
    dataMap.put("userAge", 25);

    String script = """
        (function() {
          return {
            name: userName,
            age: userAge,
            status: 'active'
          };
        })()
      """;

    Object result = JavaScriptUtil.execute(script, dataMap);
    assertNotNull(result);
    assertInstanceOf(Map.class, result);

    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertEquals("Bob", resultMap.get("name"));
    assertEquals(25, resultMap.get("age"));
    assertEquals("active", resultMap.get("status"));
  }

  @Test
  void testexecuteReturnNestedObject() throws Exception {
    // 测试脚本返回嵌套对象
    String script = """
        (function() {
          return {
            person: {
              name: 'Charlie',
              age: 35
            },
            address: {
              street: '123 Main St',
              city: 'Shanghai'
            },
            active: true
          };
        })()
      """;

    Object result = JavaScriptUtil.execute(script, new HashMap<>());
    assertNotNull(result);
    assertInstanceOf(Map.class, result);

    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;

    // 验证嵌套对象
    assertInstanceOf(Map.class, resultMap.get("person"));
    @SuppressWarnings("unchecked")
    Map<String, Object> personMap = (Map<String, Object>) resultMap.get("person");
    assertEquals("Charlie", personMap.get("name"));
    assertEquals(35, personMap.get("age"));

    assertTrue(resultMap.get("address") instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, Object> addressMap = (Map<String, Object>) resultMap.get("address");
    assertEquals("123 Main St", addressMap.get("street"));
    assertEquals("Shanghai", addressMap.get("city"));

    assertEquals(true, resultMap.get("active"));
  }

  @Test
  void testexecuteReturnObjectWithArray() throws Exception {
    // 测试脚本返回包含数组的对象
    String script = """
        (function() {
          return {
            name: 'David',
            hobbies: ['reading', 'swimming', 'coding'],
            scores: [85, 92, 78]
          };
        })()
      """;

    Object result = JavaScriptUtil.execute(script, new HashMap<>());
    assertNotNull(result);
    assertInstanceOf(Map.class, result);

    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertEquals("David", resultMap.get("name"));

    // 验证数组
    Object hobbies = resultMap.get("hobbies");
    assertInstanceOf(List.class, hobbies);
    List<?> hobbiesList = (List<?>) hobbies;
    assertEquals(3, hobbiesList.size());
    assertEquals("reading", hobbiesList.get(0));
    assertEquals("swimming", hobbiesList.get(1));
    assertEquals("coding", hobbiesList.get(2));

    Object scores = resultMap.get("scores");
    assertInstanceOf(List.class, scores);
    List<?> scoresList = (List<?>) scores;
    assertEquals(3, scoresList.size());
    assertEquals(85, scoresList.get(0));
    assertEquals(92, scoresList.get(1));
    assertEquals(78, scoresList.get(2));
  }

  @Test
  void testexecuteReturnObjectWithCalculations() throws Exception {
    // 测试脚本返回包含计算结果的对象
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("price", 100);
    dataMap.put("tax", 0.1);

    String script = """
        (function() {
          return {
            originalPrice: price,
            taxAmount: price * tax,
            totalPrice: price * (1 + tax),
            discount: 0.05,
            finalPrice: price * (1 + tax) * (1 - 0.05)
          };
        })()
      """;

    Object result = JavaScriptUtil.execute(script, dataMap);
    assertNotNull(result);
    assertInstanceOf(Map.class, result);

    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertEquals(100, resultMap.get("originalPrice"));
    assertEquals(10.0, resultMap.get("taxAmount"));
    assertEquals(110.00000000000001, resultMap.get("totalPrice"));
    assertEquals(0.05, resultMap.get("discount"));
    assertEquals(104.50000000000001, resultMap.get("finalPrice"));
  }

  @Test
  void testexecuteReturnEmptyObject() throws Exception {
    // 测试脚本返回空对象
    String script = "(function() { return {}; })()";

    Object result = JavaScriptUtil.execute(script, new HashMap<>());
    assertNotNull(result);
    assertInstanceOf(Map.class, result);

    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertTrue(resultMap.isEmpty());
  }

  /**
   * 测试身份验证脚本
   */
  @Test
  void testAuthenticate() throws Exception {
    String script = """
      /**
       * Identity Provider Script Template
       * Implement authenticate(request) to return an object:
       *   { success: boolean, user?: { id: string; name?: string; roles?: string[] }, message?: string }
       * You can read headers via request.headers and query via request.query
       */
      function authenticate(request) {
        // Example: simple token check from header
        const auth = request.headers['authorization'] || request.headers['Authorization'];
        if (!auth || !auth.startsWith('Bearer ')) {
          return { success: false, message: 'Missing bearer token' };
        }
        const token = auth.substring('Bearer '.length);
        // TODO: verify token, fetch user, etc.
        if (token === 'demo-token') {
          return { success: true, user: { id: 'demo', name: 'Demo User', roles: ['user'] } };
        }
        return { success: false, message: 'Invalid token' };
      }
      """;
    script += "authenticate(request);";
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("request", Map.of("headers", Map.of("authorization", "Bearer demo-token")));
    Object result = JavaScriptUtil.execute(script, dataMap);
    assertNotNull(result);
    Map<?, ?> resultMap = assertInstanceOf(Map.class, result);
    assertTrue((boolean) resultMap.get("success"));
  }

}
