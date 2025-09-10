package tech.wetech.flexmodel.flow.processor;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.SQLiteTestResource;
import tech.wetech.flexmodel.domain.model.flow.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.param.CreateFlowParam;
import tech.wetech.flexmodel.domain.model.flow.param.DeployFlowParam;
import tech.wetech.flexmodel.domain.model.flow.param.GetFlowModuleParam;
import tech.wetech.flexmodel.domain.model.flow.param.UpdateFlowParam;
import tech.wetech.flexmodel.domain.model.flow.processor.DefinitionProcessor;
import tech.wetech.flexmodel.domain.model.flow.result.CreateFlowResult;
import tech.wetech.flexmodel.domain.model.flow.result.DeployFlowResult;
import tech.wetech.flexmodel.domain.model.flow.result.FlowModuleResult;
import tech.wetech.flexmodel.domain.model.flow.result.UpdateFlowResult;
import tech.wetech.flexmodel.flow.EntityBuilder;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class DefinitionProcessorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionProcessor.class);
  @Inject
  DefinitionProcessor definitionProcessor;

  @Test
  public void createTest() {
    CreateFlowParam createFlowParam = EntityBuilder.buildCreateFlowParam();
    CreateFlowResult createFlowResult = definitionProcessor.create(createFlowParam);
    LOGGER.info("createFlow.||createFlowResult={}", createFlowResult);
    Assertions.assertTrue(createFlowResult.getErrCode() == ErrorEnum.SUCCESS.getErrNo());
  }

  @Test
  public void updateTest() {
    CreateFlowParam createFlowParam = EntityBuilder.buildCreateFlowParam();
    UpdateFlowParam updateFlowParam = EntityBuilder.buildUpdateFlowParam();

    CreateFlowResult createFlowResult = definitionProcessor.create(createFlowParam);
    updateFlowParam.setFlowModuleId(createFlowResult.getFlowModuleId());
    UpdateFlowResult updateFlowResult = definitionProcessor.update(updateFlowParam);
    LOGGER.info("updateFlow.||result={}", updateFlowParam);
    Assertions.assertTrue(updateFlowResult.getErrCode() == ErrorEnum.SUCCESS.getErrNo());
  }

  @Test
  public void deployTest() {
    CreateFlowParam createFlowParam = EntityBuilder.buildCreateFlowParam();
    UpdateFlowParam updateFlowParam = EntityBuilder.buildUpdateFlowParam();
    DeployFlowParam deployFlowParam = EntityBuilder.buildDeployFlowParm();

    CreateFlowResult createFlowResult = definitionProcessor.create(createFlowParam);
    updateFlowParam.setFlowModuleId(createFlowResult.getFlowModuleId());
    UpdateFlowResult updateFlowResult = definitionProcessor.update(updateFlowParam);
    Assertions.assertTrue(updateFlowResult.getErrCode() == ErrorEnum.SUCCESS.getErrNo());
    deployFlowParam.setFlowModuleId(createFlowResult.getFlowModuleId());
    DeployFlowResult deployFlowResult = definitionProcessor.deploy(deployFlowParam);
    LOGGER.info("deployFlowTest.||deployFlowResult={}", deployFlowResult);
    Assertions.assertTrue(deployFlowResult.getErrCode() == ErrorEnum.SUCCESS.getErrNo());
  }

  @Test
  public void getFlowModule() {
    CreateFlowParam createFlowParam = EntityBuilder.buildCreateFlowParam();
    UpdateFlowParam updateFlowParam = EntityBuilder.buildUpdateFlowParam();
    DeployFlowParam deployFlowParam = EntityBuilder.buildDeployFlowParm();
    GetFlowModuleParam flowModuleParam = new GetFlowModuleParam();

    CreateFlowResult createFlowResult = definitionProcessor.create(createFlowParam);
    updateFlowParam.setFlowModuleId(createFlowResult.getFlowModuleId());
    UpdateFlowResult updateFlowResult = definitionProcessor.update(updateFlowParam);
    Assertions.assertTrue(updateFlowResult.getErrCode() == ErrorEnum.SUCCESS.getErrNo());

    flowModuleParam.setFlowModuleId(updateFlowParam.getFlowModuleId());
    FlowModuleResult flowModuleResultByFlowModuleId = definitionProcessor.getFlowModule(flowModuleParam);
    Assertions.assertTrue(flowModuleResultByFlowModuleId.getFlowModuleId().equals(createFlowResult.getFlowModuleId()));

    deployFlowParam.setFlowModuleId(createFlowResult.getFlowModuleId());
    DeployFlowResult deployFlowResult = definitionProcessor.deploy(deployFlowParam);
    flowModuleParam.setFlowDeployId(deployFlowResult.getFlowDeployId());
    flowModuleParam.setFlowModuleId(null);
    FlowModuleResult flowModuleResultByDeployId = definitionProcessor.getFlowModule(flowModuleParam);
    Assertions.assertTrue(flowModuleResultByDeployId.getFlowModel().equals(updateFlowParam.getFlowModel()));
  }

}
