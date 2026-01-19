package dev.flexmodel.domain.model.flow.executor;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.dto.model.FlowModel;
import dev.flexmodel.domain.model.flow.shared.EntityBuilder;
import dev.flexmodel.domain.model.flow.shared.common.RuntimeContext;
import dev.flexmodel.domain.model.flow.shared.util.FlowModelUtil;
import dev.flexmodel.shared.utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class ExclusiveGatewayExecutorTest {

  @Inject
  ExecutorFactory executorFactory;

  private ExclusiveGatewayExecutor exclusiveGatewayExecutor;

  private RuntimeContext runtimeContext;

  @BeforeEach
  public void initExclusiveGatewayExecutor() {
    List<FlowElement> flowElementList = EntityBuilder.buildFlowElementList();

    FlowModel flowModel = new FlowModel();
    flowModel.setFlowElementList(flowElementList);
    Map<String, FlowElement> flowElementMap = FlowModelUtil.getFlowElementMap(JsonUtils.getInstance().stringify(flowModel));

    FlowElement exclusiveGateway = FlowModelUtil.getFlowElement(flowElementMap, "exclusiveGateway1");

    runtimeContext = EntityBuilder.buildRuntimeContext();
    Map<String, Object> instanceDataMap = new HashMap<>();
    instanceDataMap.put("a", 2);
    instanceDataMap.put("b", 1);
    runtimeContext.setInstanceDataMap(instanceDataMap);
    runtimeContext.setCurrentNodeModel(exclusiveGateway);

    try {
      exclusiveGatewayExecutor = (ExclusiveGatewayExecutor) executorFactory
        .getElementExecutor(exclusiveGateway);
    } catch (Exception e) {
      log.error("", e);
    }
  }

  @Test
  public void testDoExecute() {
    try {
      exclusiveGatewayExecutor.doExecute(runtimeContext);
    } catch (Exception e) {
      log.error("", e);
    }
  }

  @Test
  public void testGetExecuteExecutor() {
    try {
      exclusiveGatewayExecutor.getExecuteExecutor(runtimeContext);
      String Key = runtimeContext.getCurrentNodeModel().getKey();
      Assertions.assertEquals("userTask2", Key);
    } catch (Exception e) {
      log.error("", e);
    }
  }
}
