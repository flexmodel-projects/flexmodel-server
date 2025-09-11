package tech.wetech.flexmodel.domain.model.flow.executor;

import com.google.common.collect.Maps;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.SQLiteTestResource;
import tech.wetech.flexmodel.domain.model.flow.dto.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.dto.model.FlowModel;
import tech.wetech.flexmodel.domain.model.flow.dto.model.InstanceData;
import tech.wetech.flexmodel.domain.model.flow.shared.common.RuntimeContext;
import tech.wetech.flexmodel.domain.model.flow.shared.util.FlowModelUtil;
import tech.wetech.flexmodel.domain.model.flow.util.EntityBuilder;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.util.List;
import java.util.Map;

@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class ExclusiveGatewayExecutorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExclusiveGatewayExecutorTest.class);

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
    Map<String, InstanceData> instanceDataMap = Maps.newHashMap();
    InstanceData instanceDataA = new InstanceData("a", 2);
    InstanceData instanceDataB = new InstanceData("b", 1);
    instanceDataMap.put(instanceDataA.getKey(), instanceDataA);
    instanceDataMap.put(instanceDataB.getKey(), instanceDataB);
    runtimeContext.setInstanceDataMap(instanceDataMap);
    runtimeContext.setCurrentNodeModel(exclusiveGateway);

    try {
      exclusiveGatewayExecutor = (ExclusiveGatewayExecutor) executorFactory
        .getElementExecutor(exclusiveGateway);
    } catch (Exception e) {
      LOGGER.error("", e);
    }
  }

  @Test
  public void testDoExecute() {
    try {
      exclusiveGatewayExecutor.doExecute(runtimeContext);
    } catch (Exception e) {
      LOGGER.error("", e);
    }
  }

  @Test
  public void testGetExecuteExecutor() {
    try {
      exclusiveGatewayExecutor.getExecuteExecutor(runtimeContext);
      String modelKey = runtimeContext.getCurrentNodeModel().getKey();
      Assertions.assertEquals("userTask2", modelKey);
    } catch (Exception e) {
      LOGGER.error("", e);
    }
  }
}
