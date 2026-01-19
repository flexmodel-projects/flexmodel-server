package dev.flexmodel.domain.model.flow.executor;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;
import dev.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.domain.model.flow.shared.common.NodeInstanceStatus;
import dev.flexmodel.domain.model.flow.shared.common.RuntimeContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServiceTaskExecutor测试用例
 * js脚本示例：
 * {
 *     "key": "service-task-js",
 *     "type": 5,
 *     "incoming": [],
 *     "outgoing": [],
 *     "properties": {
 *         "subType": "script",
 *         "script": "return 'Hello, World!';"
 *     }
 * }
 *
 * @author cjbi
 */
@Slf4j
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
class ServiceTaskExecutorTest {

  @Inject
  ServiceTaskExecutor serviceTaskExecutor;
  private RuntimeContext runtimeContext;
  private NodeInstanceBO nodeInstance;

  @BeforeEach
  void setUp() {

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
  void testExecuteJavaScriptScript() throws ProcessException {
    // 准备测试数据
    nodeInstance.put("subType", "script");
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
  void testExecuteWithJavaScriptSyntaxError() {
    // 准备测试数据 - JavaScript语法错误
    nodeInstance.put("subType", "script");
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

  // ========== 边界情况测试 ==========

  @Test
  void testExecuteWithJavaScriptDivisionByZero() throws ProcessException {
    // 准备测试数据 - JavaScript除零
    nodeInstance.put("subType", "script");
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
  void testExecuteWithJavaScriptRuntimeException() {
    // 准备测试数据 - JavaScript运行时异常
    nodeInstance.put("subType", "script");
    nodeInstance.put("script", "throw new Error('Test error')");

    // 执行测试并验证异常
    ProcessException exception = assertThrows(ProcessException.class, () -> {
      serviceTaskExecutor.doExecute(runtimeContext);
    });

    assertEquals(ErrorEnum.MISSING_DATA.getErrNo(), exception.getErrNo());
    assertTrue(exception.getMessage().contains("Error"));
  }
}
