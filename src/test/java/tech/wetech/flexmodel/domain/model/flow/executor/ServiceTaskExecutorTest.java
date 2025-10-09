package tech.wetech.flexmodel.domain.model.flow.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import tech.wetech.flexmodel.domain.model.flow.dto.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;
import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.shared.common.NodeInstanceStatus;
import tech.wetech.flexmodel.domain.model.flow.shared.common.RuntimeContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServiceTaskExecutor测试用例
 * groovy脚本示例：
 * {
 *     "key": "service-task-groovy",
 *     "type": 5,
 *     "incoming": [],
 *     "outgoing": [],
 *     "properties": {
 *         "subType": "groovy",
 *         "script": "return 'Hello, World!'"
 *     }
 * }
 * ---
 * js脚本示例：
 * {
 *     "key": "service-task-js",
 *     "type": 5,
 *     "incoming": [],
 *     "outgoing": [],
 *     "properties": {
 *         "subType": "js",
 *         "script": "return 'Hello, World!';"
 *     }
 * }
 *
 * @author cjbi
 */
class ServiceTaskExecutorTest {

  private ServiceTaskExecutor serviceTaskExecutor;
  private RuntimeContext runtimeContext;
  private NodeInstanceBO nodeInstance;

  @BeforeEach
  void setUp() {
    serviceTaskExecutor = new ServiceTaskExecutor();

    // 创建测试用的RuntimeContext
    runtimeContext = new RuntimeContext();
    runtimeContext.setFlowInstanceId("flow-001");
    runtimeContext.setInstanceDataId("data-001");
    runtimeContext.setInstanceDataMap(new HashMap<>());
    runtimeContext.setExtendProperties(new HashMap<>());
    runtimeContext.setNodeInstanceList(new ArrayList<>());

    // 创建测试用的FlowElement
    FlowElement flowElement = new FlowElement();
    flowElement.setKey("serviceTask1");
    flowElement.setType(5); // SERVICE_TASK类型
    flowElement.setOutgoing(new ArrayList<>());
    flowElement.setProperties(new HashMap<>());
    runtimeContext.setCurrentNodeModel(flowElement);

    // 创建测试用的NodeInstanceBO
    nodeInstance = new NodeInstanceBO();
    nodeInstance.setNodeInstanceId("node-001");
    nodeInstance.setNodeKey("serviceTask1");
    nodeInstance.setNodeType(5);
    nodeInstance.setStatus(NodeInstanceStatus.ACTIVE);
    nodeInstance.setProperties(new HashMap<>());

    runtimeContext.setCurrentNodeInstance(nodeInstance);
  }

  // ========== 基本功能测试 ==========

  @Test
  void testExecuteGroovyScript() throws ProcessException {
    // 准备测试数据
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", "return a + b");

    Map<String, Object> instanceDataMap = new HashMap<>();
    instanceDataMap.put("a", 10);
    instanceDataMap.put("b", 20);
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果 - 执行结果存储在instanceDataMap中
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));
    assertEquals(30, runtimeContext.getInstanceDataMap().get("executionResult"));
    assertTrue(runtimeContext.getNodeInstanceList().contains(nodeInstance));
  }

  @Test
  void testExecuteJavaScriptScript() throws ProcessException {
    // 准备测试数据
    nodeInstance.put("subType", "js");
    nodeInstance.put("script", "var result = x * y; result;");

    Map<String, Object> instanceDataMap = new HashMap<>();
    instanceDataMap.put("x", 5);
    instanceDataMap.put("y", 6);
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果 - 执行结果存储在instanceDataMap中
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));
    assertEquals(30, runtimeContext.getInstanceDataMap().get("executionResult"));
    assertTrue(runtimeContext.getNodeInstanceList().contains(nodeInstance));
  }

  @Test
  void testExecuteWithMapResult() throws ProcessException {
    // 准备测试数据 - 返回Map结果
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", "return [result: 'success', value: 100]");

    Map<String, Object> instanceDataMap = new HashMap<>();
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果 - Map结果合并到instanceDataMap中
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));
    assertEquals("success", runtimeContext.getInstanceDataMap().get("result"));
    assertEquals(100, runtimeContext.getInstanceDataMap().get("value"));
  }

  @Test
  void testExecuteWithNullResult() throws ProcessException {
    // 准备测试数据 - 返回null
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", "return null");

    Map<String, Object> instanceDataMap = new HashMap<>();
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNull(runtimeContext.getInstanceDataMap().get("executionResult"));
  }

  @Test
  void testExecuteWithDifferentSubTypes() throws ProcessException {
    // 测试不同的subType大小写 - 只测试支持的subType
    String[] subTypes = {"groovy", "GROOVY", "js", "JS"};

    for (String subType : subTypes) {
      // 重新设置测试数据
      nodeInstance.setStatus(NodeInstanceStatus.ACTIVE);
      nodeInstance.put("subType", subType);
      nodeInstance.put("script", "'test'");

      Map<String, Object> instanceDataMap = new HashMap<>();
      runtimeContext.setInstanceDataMap(instanceDataMap);

      // 执行测试
      serviceTaskExecutor.doExecute(runtimeContext);

      // 验证结果
      assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
      assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));
      assertEquals("test", runtimeContext.getInstanceDataMap().get("executionResult"));
    }
  }

  @Test
  void testExecuteWithMissingScript() {
    // 准备测试数据 - 缺少script
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", null);

    // 执行测试并验证异常
    ProcessException exception = assertThrows(ProcessException.class, () -> {
      serviceTaskExecutor.doExecute(runtimeContext);
    });

    assertEquals(ErrorEnum.MISSING_DATA.getErrNo(), exception.getErrNo());
    assertTrue(exception.getMessage().contains("Miss data"));
  }

  @Test
  void testExecuteWithEmptyScript() {
    // 准备测试数据 - 空脚本
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", "   ");

    // 执行测试并验证异常
    ProcessException exception = assertThrows(ProcessException.class, () -> {
      serviceTaskExecutor.doExecute(runtimeContext);
    });

    assertEquals(ErrorEnum.MISSING_DATA.getErrNo(), exception.getErrNo());
    assertTrue(exception.getMessage().contains("Miss data"));
  }

  @Test
  void testExecuteWithUnsupportedSubType() {
    // 准备测试数据 - 不支持的subType
    nodeInstance.put("subType", "python");
    nodeInstance.put("script", "print('hello')");

    // 执行测试并验证异常
    ProcessException exception = assertThrows(ProcessException.class, () -> {
      serviceTaskExecutor.doExecute(runtimeContext);
    });

    assertEquals(ErrorEnum.UNSUPPORTED_ELEMENT_TYPE.getErrNo(), exception.getErrNo());
    assertTrue(exception.getMessage().contains("Unsupported element type"));
  }

  @Test
  void testExecuteWithGroovySyntaxError() {
    // 准备测试数据 - Groovy语法错误
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", "invalid groovy syntax {");

    // 执行测试并验证异常
    ProcessException exception = assertThrows(ProcessException.class, () -> {
      serviceTaskExecutor.doExecute(runtimeContext);
    });

    assertEquals(ErrorEnum.SERVICE_TASK_EXECUTION_FAILED.getErrNo(), exception.getErrNo());
    assertTrue(exception.getMessage().contains("服务任务执行失败"));
  }

  @Test
  void testExecuteWithJavaScriptSyntaxError() {
    // 准备测试数据 - JavaScript语法错误
    nodeInstance.put("subType", "js");
    nodeInstance.put("script", "var x = { invalid syntax");

    // 执行测试并验证异常
    ProcessException exception = assertThrows(ProcessException.class, () -> {
      serviceTaskExecutor.doExecute(runtimeContext);
    });

    assertEquals(ErrorEnum.MISSING_DATA.getErrNo(), exception.getErrNo());
    assertTrue(exception.getMessage().contains("SyntaxError"));
  }

  @Test
  void testExecuteWithNullCurrentNodeInstance() {
    // 准备测试数据 - null当前节点实例
    runtimeContext.setCurrentNodeInstance(null);

    // 执行测试并验证异常
    ProcessException exception = assertThrows(ProcessException.class, () -> {
      serviceTaskExecutor.doExecute(runtimeContext);
    });

    assertEquals(ErrorEnum.MISSING_DATA.getErrNo(), exception.getErrNo());
    assertTrue(exception.getMessage().contains("Miss data"));
  }

  @Test
  void testExecuteWithAlreadyCompletedNode() throws ProcessException {
    // 准备测试数据 - 已完成节点
    nodeInstance.setStatus(NodeInstanceStatus.COMPLETED);
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", "'test'");

    // 执行测试 - 应该直接返回，不执行
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果 - 状态保持为COMPLETED
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
  }

  // ========== 边界情况测试 ==========

  @Test
  void testExecuteWithVeryLongScript() throws ProcessException {
    // 准备测试数据 - 超长脚本
    StringBuilder longScript = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longScript.append("def var").append(i).append(" = ").append(i).append("; ");
    }
    longScript.append("return var999;");

    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", longScript.toString());

    Map<String, Object> instanceDataMap = new HashMap<>();
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));
    assertEquals(999, runtimeContext.getInstanceDataMap().get("executionResult"));
  }

  @Test
  void testExecuteWithSpecialCharacters() throws ProcessException {
    // 准备测试数据 - 包含特殊字符的脚本
    String scriptWithSpecialChars = "return '特殊字符: !@#$%^&*()_+-=[]{}|;:,.<>?/~`'";

    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", scriptWithSpecialChars);

    Map<String, Object> instanceDataMap = new HashMap<>();
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));
    assertEquals("特殊字符: !@#$%^&*()_+-=[]{}|;:,.<>?/~`", runtimeContext.getInstanceDataMap().get("executionResult"));
  }

  @Test
  void testExecuteWithUnicodeCharacters() throws ProcessException {
    // 准备测试数据 - 包含Unicode字符的脚本
    String unicodeScript = "return 'Unicode测试: 中文 日本語 한국어 العربية עברית русский'";

    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", unicodeScript);

    Map<String, Object> instanceDataMap = new HashMap<>();
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));
    assertEquals("Unicode测试: 中文 日本語 한국어 العربية עברית русский", runtimeContext.getInstanceDataMap().get("executionResult"));
  }

  @Test
  void testExecuteWithJavaScriptDivisionByZero() throws ProcessException {
    // 准备测试数据 - JavaScript除零
    nodeInstance.put("subType", "js");
    nodeInstance.put("script", "1 / 0");

    Map<String, Object> instanceDataMap = new HashMap<>();
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // JavaScript中除零返回Infinity，不会抛出异常
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));
    assertEquals(Double.POSITIVE_INFINITY, runtimeContext.getInstanceDataMap().get("executionResult"));
  }

  @Test
  void testExecuteWithGroovyRuntimeException() {
    // 准备测试数据 - Groovy运行时异常
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", "throw new RuntimeException('Test exception')");

    // 执行测试并验证异常
    ProcessException exception = assertThrows(ProcessException.class, () -> {
      serviceTaskExecutor.doExecute(runtimeContext);
    });

    assertEquals(ErrorEnum.SERVICE_TASK_EXECUTION_FAILED.getErrNo(), exception.getErrNo());
    assertTrue(exception.getMessage().contains("服务任务执行失败"));
  }

  @Test
  void testExecuteWithJavaScriptRuntimeException() {
    // 准备测试数据 - JavaScript运行时异常
    nodeInstance.put("subType", "js");
    nodeInstance.put("script", "throw new Error('Test error')");

    // 执行测试并验证异常
    ProcessException exception = assertThrows(ProcessException.class, () -> {
      serviceTaskExecutor.doExecute(runtimeContext);
    });

    assertEquals(ErrorEnum.MISSING_DATA.getErrNo(), exception.getErrNo());
    assertTrue(exception.getMessage().contains("Error"));
  }

  @Test
  void testExecuteWithComplexDataStructures() throws ProcessException {
    // 准备测试数据 - 复杂数据结构
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", """
        def complexData = [
            numbers: [1, 2, 3, 4, 5],
            nested: [
                level1: [
                    level2: [
                        value: 'deep'
                    ]
                ]
            ],
            functions: [
                add: { a, b -> a + b },
                multiply: { a, b -> a * b }
            ]
        ]
        return complexData
        """);

    Map<String, Object> instanceDataMap = new HashMap<>();
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));

    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) runtimeContext.getInstanceDataMap().get("executionResult");
    assertNotNull(result.get("numbers"));
    assertNotNull(result.get("nested"));
    assertNotNull(result.get("functions"));
  }

  @Test
  void testExecuteWithMemoryIntensiveOperation() throws ProcessException {
    // 准备测试数据 - 内存密集型操作
    nodeInstance.put("subType", "groovy");
    nodeInstance.put("script", "def list = []; for(int i = 0; i < 10000; i++) { list.add(i) }; return list.size()");

    Map<String, Object> instanceDataMap = new HashMap<>();
    runtimeContext.setInstanceDataMap(instanceDataMap);

    // 执行测试
    serviceTaskExecutor.doExecute(runtimeContext);

    // 验证结果
    assertEquals(NodeInstanceStatus.COMPLETED, nodeInstance.getStatus());
    assertNotNull(runtimeContext.getInstanceDataMap().get("executionResult"));
    assertEquals(10000, runtimeContext.getInstanceDataMap().get("executionResult"));
  }
}
