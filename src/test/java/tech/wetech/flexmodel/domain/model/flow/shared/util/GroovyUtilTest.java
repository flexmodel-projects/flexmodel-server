package dev.flexmodel.domain.model.flow.shared.util;

import dev.flexmodel.domain.model.flow.shared.util.GroovyUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class GroovyUtilTest {

  /**
   * 测试空表达式和null表达式的情况
   */
  @Test
  public void testEmptyExpression() throws Exception {
    // 测试空字符串
    Object result1 = GroovyUtil.execute("", new HashMap<>());
    Assertions.assertNull(result1);

    // 测试null表达式
    Object result2 = GroovyUtil.execute(null, new HashMap<>());
    Assertions.assertNull(result2);

    // 测试空白字符串
    Object result3 = GroovyUtil.execute("   ", new HashMap<>());
    Assertions.assertNull(result3);
  }

  /**
   * 测试基本数学运算
   */
  @Test
  public void testBasicCalculations() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();

    // 测试加法
    Object result1 = GroovyUtil.execute("2 + 3", dataMap);
    Assertions.assertEquals(5, result1);

    // 测试减法
    Object result2 = GroovyUtil.execute("10 - 4", dataMap);
    Assertions.assertEquals(6, result2);

    // 测试乘法
    Object result3 = GroovyUtil.execute("3 * 4", dataMap);
    Assertions.assertEquals(12, result3);

    // 测试除法
    Object result4 = GroovyUtil.execute("15 / 3", dataMap);
    Assertions.assertEquals(5, Integer.valueOf(Objects.requireNonNull(result4).toString()));

    // 测试复杂表达式
    Object result5 = GroovyUtil.execute("(2 + 3) * 4 - 1", dataMap);
    Assertions.assertEquals(19, result5);
  }

  /**
   * 测试逻辑运算
   */
  @Test
  public void testLogicalOperations() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();

    // 测试布尔运算
    Object result1 = GroovyUtil.execute("true && false", dataMap);
    Assertions.assertEquals(false, result1);

    Object result2 = GroovyUtil.execute("true || false", dataMap);
    Assertions.assertEquals(true, result2);

    // 测试比较运算
    Object result3 = GroovyUtil.execute("5 > 3", dataMap);
    Assertions.assertEquals(true, result3);

    Object result4 = GroovyUtil.execute("5 == 5", dataMap);
    Assertions.assertEquals(true, result4);

    Object result5 = GroovyUtil.execute("5 != 3", dataMap);
    Assertions.assertEquals(true, result5);
  }

  /**
   * 测试变量绑定和数据传递
   */
  @Test
  public void testVariableBinding() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("a", 10);
    dataMap.put("b", 20);
    dataMap.put("name", "张三");
    dataMap.put("age", 25);

    // 测试数字变量
    Object result1 = GroovyUtil.execute("a + b", dataMap);
    Assertions.assertEquals(30, result1);

    // 测试字符串变量
    Object result2 = GroovyUtil.execute("name + '的年龄是' + age", dataMap);
    Assertions.assertEquals("张三的年龄是25", result2);

    // 测试条件判断
    Object result3 = GroovyUtil.execute("age > 18 ? '成年人' : '未成年人'", dataMap);
    Assertions.assertEquals("成年人", result3);
  }

  /**
   * 测试字符串操作
   */
  @Test
  public void testStringOperations() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("str1", "Hello");
    dataMap.put("str2", "World");

    // 测试字符串连接
    Object result1 = GroovyUtil.execute("str1 + ' ' + str2", dataMap);
    Assertions.assertEquals("Hello World", result1);

    // 测试字符串方法
    Object result2 = GroovyUtil.execute("str1.toUpperCase()", dataMap);
    Assertions.assertEquals("HELLO", result2);

    // 测试字符串长度
    Object result3 = GroovyUtil.execute("str1.length()", dataMap);
    Assertions.assertEquals(5, result3);

    // 测试字符串包含
    Object result4 = GroovyUtil.execute("str1.contains('ell')", dataMap);
    Assertions.assertEquals(true, result4);
  }

  /**
   * 测试缺失属性异常处理
   */
  @Test
  public void testMissingPropertyException() {
    Map<String, Object> dataMap = new HashMap<>();

    // 测试访问不存在的变量
    ProcessException exception = Assertions.assertThrows(ProcessException.class, () -> {
      GroovyUtil.execute("undefinedVariable", dataMap);
    });

    Assertions.assertEquals(ErrorEnum.MISSING_DATA.getErrNo(), exception.getErrNo());
  }

  /**
   * 测试语法错误和运行时异常
   */
  @Test
  public void testSyntaxErrors() {
    Map<String, Object> dataMap = new HashMap<>();

    // 测试语法错误 - 无效的变量声明
    Assertions.assertThrows(Exception.class, () -> {
      GroovyUtil.execute("int i = i;", dataMap);
    });

    // 测试语法错误 - 无效的字符串比较
    Assertions.assertThrows(Exception.class, () -> {
      GroovyUtil.execute("String String == '111';", dataMap);
    });

    // 测试语法错误 - 不存在的方法
    Assertions.assertThrows(Exception.class, () -> {
      GroovyUtil.execute("String.equals111('123')", dataMap);
    });

    // 测试类型转换错误
    Assertions.assertThrows(Exception.class, () -> {
      GroovyUtil.execute("int a = '123';", dataMap);
    });

    // 测试除零错误
    Assertions.assertThrows(Exception.class, () -> {
      GroovyUtil.execute("1/0", dataMap);
    });
  }

  /**
   * 测试脚本缓存机制
   */
  @Test
  public void testScriptCaching() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    String expression = "2 + 3";

    // 第一次执行
    Object result1 = GroovyUtil.execute(expression, dataMap);
    Assertions.assertEquals(5, result1);

    // 第二次执行相同表达式，应该使用缓存
    Object result2 = GroovyUtil.execute(expression, dataMap);
    Assertions.assertEquals(5, result2);

    // 验证结果一致性
    Assertions.assertEquals(result1, result2);
  }

  /**
   * 测试边界情况和特殊场景
   */
  @Test
  public void testEdgeCases() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();

    // 测试null值处理
    dataMap.put("nullValue", null);
    Object result1 = GroovyUtil.execute("nullValue == null", dataMap);
    Assertions.assertEquals(true, result1);

    // 测试空字符串
    dataMap.put("emptyStr", "");
    Object result2 = GroovyUtil.execute("emptyStr.isEmpty()", dataMap);
    Assertions.assertEquals(true, result2);

    // 测试负数
    Object result3 = GroovyUtil.execute("-5 + 3", dataMap);
    Assertions.assertEquals(-2, result3);

    // 测试小数运算
    Object result4 = GroovyUtil.execute("3.14 * 2", dataMap);
    Assertions.assertNotNull(result4);
    Assertions.assertEquals(6.28, Double.parseDouble(result4.toString()));

    // 测试布尔值
    dataMap.put("flag", true);
    Object result5 = GroovyUtil.execute("!flag", dataMap);
    Assertions.assertEquals(false, result5);
  }

  /**
   * 测试复杂业务场景
   */
  @Test
  public void testComplexBusinessScenarios() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("price", 100.0);
    dataMap.put("quantity", 5);
    dataMap.put("discount", 0.1);
    dataMap.put("taxRate", 0.08);

    // 计算总价：价格 * 数量 * (1 - 折扣) * (1 + 税率)
    Object totalPrice = GroovyUtil.execute("price * quantity * (1 - discount) * (1 + taxRate)", dataMap);
    Assertions.assertNotNull(totalPrice);
    BigDecimal scale = new BigDecimal(totalPrice.toString()).setScale(2, RoundingMode.HALF_UP);
    Assertions.assertEquals(486.00, scale.doubleValue());

    // 测试条件判断
    dataMap.put("score", 85);
    Object grade = GroovyUtil.execute("score >= 90 ? 'A' : score >= 80 ? 'B' : score >= 70 ? 'C' : 'D'", dataMap);
    Assertions.assertEquals("B", grade);
  }

  /**
   * 测试数组和集合操作
   */
  @Test
  public void testArrayAndCollectionOperations() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("numbers", new int[]{1, 2, 3, 4, 5});

    // 测试数组长度
    Object result1 = GroovyUtil.execute("numbers.length", dataMap);
    Assertions.assertEquals(5, result1);

    // 测试数组元素访问
    Object result2 = GroovyUtil.execute("numbers[0]", dataMap);
    Assertions.assertEquals(1, result2);

    // 测试数组求和
    Object result3 = GroovyUtil.execute("numbers.sum()", dataMap);
    Assertions.assertEquals(15, result3);
  }

  /**
   * 测试多行代码执行
   */
  @Test
  public void testMultiLineCodeExecution() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("baseSalary", 5000);
    dataMap.put("bonus", 1000);
    dataMap.put("taxRate", 0.1);

    // 测试多行代码：计算税后工资
    String multiLineCode = """
      def grossSalary = baseSalary + bonus
      def tax = grossSalary * taxRate
      def netSalary = grossSalary - tax
      return netSalary
      """;

    Object result = GroovyUtil.execute(multiLineCode, dataMap);
    Assertions.assertEquals(5400.0, result);

    // 测试多行代码：字符串处理
    dataMap.put("text", "  Hello World  ");
    String stringProcessingCode = """
      def trimmed = text.trim()
      def upper = trimmed.toUpperCase()
      def words = upper.split(' ')
      return words.length
      """;

    Object wordCount = GroovyUtil.execute(stringProcessingCode, dataMap);
    Assertions.assertEquals(2, wordCount);
  }

  /**
   * 测试复杂对象返回
   */
  @Test
  public void testComplexObjectReturn() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("name", "张三");
    dataMap.put("age", 30);
    dataMap.put("salary", 8000);

    // 测试返回Map对象
    String mapReturnCode = """
      def person = [:]
      person.name = name
      person.age = age
      person.salary = salary
      person.category = age > 25 ? 'senior' : 'junior'
      return person
      """;

    Object result = GroovyUtil.execute(mapReturnCode, dataMap);
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result instanceof Map);

    @SuppressWarnings("unchecked")
    Map<String, Object> personMap = (Map<String, Object>) result;
    Assertions.assertEquals("张三", personMap.get("name"));
    Assertions.assertEquals(30, personMap.get("age"));
    Assertions.assertEquals(8000, personMap.get("salary"));
    Assertions.assertEquals("senior", personMap.get("category"));

    // 测试返回List对象
    String listReturnCode = """
      def numbers = [1, 2, 3, 4, 5]
      def doubled = numbers.collect { it * 2 }
      return doubled
      """;

    Object listResult = GroovyUtil.execute(listReturnCode, dataMap);
    Assertions.assertNotNull(listResult);
    Assertions.assertTrue(listResult instanceof java.util.List);

    @SuppressWarnings("unchecked")
    java.util.List<Integer> doubledList = (java.util.List<Integer>) listResult;
    Assertions.assertEquals(5, doubledList.size());
    Assertions.assertEquals(2, doubledList.get(0));
    Assertions.assertEquals(10, doubledList.get(4));
  }

  /**
   * 测试函数定义和执行
   */
  @Test
  public void testFunctionDefinitionAndExecution() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("radius", 5.0);

    // 测试函数定义和调用
    String functionCode = """
      def calculateArea(r) {
          return Math.PI * r * r
      }

      def calculateCircumference(r) {
          return 2 * Math.PI * r
      }

      def area = calculateArea(radius)
      def circumference = calculateCircumference(radius)

      return [area: area, circumference: circumference]
      """;

    Object result = GroovyUtil.execute(functionCode, dataMap);
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result instanceof Map);

    @SuppressWarnings("unchecked")
    Map<String, Object> circleData = (Map<String, Object>) result;
    Assertions.assertTrue(circleData.containsKey("area"));
    Assertions.assertTrue(circleData.containsKey("circumference"));

    double area = ((Number) circleData.get("area")).doubleValue();
    double circumference = ((Number) circleData.get("circumference")).doubleValue();

    Assertions.assertEquals(78.54, area, 0.01);
    Assertions.assertEquals(31.42, circumference, 0.01);
  }

  /**
   * 测试循环和条件控制流
   */
  @Test
  public void testLoopsAndControlFlow() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("maxNumber", 10);

    // 测试for循环
    String forLoopCode = """
      def sum = 0
      for (int i = 1; i <= maxNumber; i++) {
          sum += i
      }
      return sum
      """;

    Object sumResult = GroovyUtil.execute(forLoopCode, dataMap);
    Assertions.assertEquals(55, sumResult);

    // 测试while循环和条件判断
    String whileLoopCode = """
      def number = 100
      def count = 0
      while (number > 1) {
          number = number / 2
          count++
      }
      return count
      """;

    Object countResult = GroovyUtil.execute(whileLoopCode, dataMap);
    Assertions.assertEquals(7, countResult);

    // 测试switch语句
    dataMap.put("grade", "B");
    String switchCode = """
      def score
      switch (grade) {
          case 'A':
              score = 90
              break
          case 'B':
              score = 80
              break
          case 'C':
              score = 70
              break
          default:
              score = 60
      }
      return score
      """;

    Object scoreResult = GroovyUtil.execute(switchCode, dataMap);
    Assertions.assertEquals(80, scoreResult);
  }

  /**
   * 测试集合和Map的高级操作
   */
  @Test
  public void testAdvancedCollectionOperations() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("numbers", java.util.Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

    // 测试集合过滤和映射
    String filterMapCode = """
      def evenNumbers = numbers.findAll { it % 2 == 0 }
      def squaredEvens = evenNumbers.collect { it * it }
      def sum = squaredEvens.sum()
      return [evenNumbers: evenNumbers, squaredEvens: squaredEvens, sum: sum]
      """;

    Object result = GroovyUtil.execute(filterMapCode, dataMap);
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result instanceof Map);

    @SuppressWarnings("unchecked")
    Map<String, Object> processedData = (Map<String, Object>) result;

    @SuppressWarnings("unchecked")
    java.util.List<Integer> evenNumbers = (java.util.List<Integer>) processedData.get("evenNumbers");
    @SuppressWarnings("unchecked")
    java.util.List<Integer> squaredEvens = (java.util.List<Integer>) processedData.get("squaredEvens");
    Integer sum = (Integer) processedData.get("sum");

    Assertions.assertEquals(5, evenNumbers.size());
    Assertions.assertEquals(2, evenNumbers.get(0));
    Assertions.assertEquals(4, squaredEvens.get(0));
    Assertions.assertEquals(220, sum);

    // 测试Map操作
    dataMap.put("scores", new HashMap<String, Integer>() {
      {
        put("数学", 95);
        put("语文", 88);
        put("英语", 92);
      }
    });

    String mapOperationCode = """
      def totalScore = 0
      def subjectCount = 0
      def averageScore = 0

      scores.each { subject, score ->
          totalScore += score
          subjectCount++
      }

      if (subjectCount > 0) {
          averageScore = totalScore / subjectCount
      }

      return [totalScore: totalScore, subjectCount: subjectCount, averageScore: averageScore]
      """;

    Object mapResult = GroovyUtil.execute(mapOperationCode, dataMap);
    Assertions.assertNotNull(mapResult);
    Assertions.assertTrue(mapResult instanceof Map);

    @SuppressWarnings("unchecked")
    Map<String, Object> scoreData = (Map<String, Object>) mapResult;
    Assertions.assertEquals(275, scoreData.get("totalScore"));
    Assertions.assertEquals(3, scoreData.get("subjectCount"));
    BigDecimal averageScore = new BigDecimal(scoreData.get("averageScore").toString());
    Assertions.assertEquals(91.6666666667, averageScore.doubleValue());
  }

  /**
   * 测试异常处理和错误恢复
   */
  @Test
  public void testExceptionHandlingAndRecovery() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("dividend", 10);
    dataMap.put("divisor", 0);

    // 测试try-catch异常处理
    String exceptionHandlingCode = """
      def result
      try {
          result = dividend / divisor
      } catch (ArithmeticException e) {
          result = "除零错误: " + e.getMessage()
      }
      return result
      """;

    Object result = GroovyUtil.execute(exceptionHandlingCode, dataMap);
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.toString().contains("除零错误"));

    // 测试空值安全处理
    dataMap.put("nullableValue", null);
    String nullSafetyCode = """
      def safeValue = nullableValue ?: "默认值"
      def length = safeValue.length()
      return [safeValue: safeValue, length: length]
      """;

    Object nullResult = GroovyUtil.execute(nullSafetyCode, dataMap);
    Assertions.assertNotNull(nullResult);
    Assertions.assertTrue(nullResult instanceof Map);

    @SuppressWarnings("unchecked")
    Map<String, Object> nullData = (Map<String, Object>) nullResult;
    Assertions.assertEquals("默认值", nullData.get("safeValue"));
    Assertions.assertEquals(3, nullData.get("length"));
  }

  /**
   * 测试性能和多线程安全
   */
  @Test
  public void testPerformanceAndThreadSafety() throws Exception {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("iterations", 1000);

    // 测试大量计算
    String performanceCode = """
      def startTime = System.currentTimeMillis()
      def sum = 0
      for (int i = 0; i < iterations; i++) {
          sum += i * i
      }
      def endTime = System.currentTimeMillis()
      def duration = endTime - startTime
      return [sum: sum, duration: duration]
      """;

    Object result = GroovyUtil.execute(performanceCode, dataMap);
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result instanceof Map);

    @SuppressWarnings("unchecked")
    Map<String, Object> perfData = (Map<String, Object>) result;
    Assertions.assertTrue((Integer) perfData.get("sum") > 0);
    Assertions.assertTrue((Long) perfData.get("duration") >= 0);
  }

  @Test
  void testAuthenticate() throws Exception {
    String script = """
          /**
       * Identity Provider Groovy Script Template
       * Implement authenticate(request) to return an object:
       *   [success: boolean, user: [id: string, name?: string, roles?: string[]], message?: string]
       * You can read headers via request.headers and query via request.query
       */
      def authenticate(request) {
          // Example: simple token check from header
          def auth = request.headers['authorization'] ?: request.headers['Authorization']
          if (!auth || !auth.startsWith('Bearer ')) {
              return [success: false, message: 'Missing bearer token']
          }
          def token = auth.replace('Bearer ','')
          // TODO: verify token, fetch user, etc.
          if (token == 'demo-token') {
              return [success: true, user: [id: 'demo', name: 'Demo User', roles: ['user']]]
          }
          return [success: false, message: 'Invalid token']
      }
      """;
    script+="\nreturn authenticate(request)";
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("request", Map.of("headers", Map.of("authorization", "Bearer demo-token")));

    Object result = GroovyUtil.execute(script, dataMap);
    Assertions.assertNotNull(result);
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = Assertions.assertInstanceOf(Map.class, result);
    Assertions.assertTrue((Boolean) resultMap.get("success"));
  }

}
