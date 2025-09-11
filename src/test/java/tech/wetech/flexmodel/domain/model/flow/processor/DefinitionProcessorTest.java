package tech.wetech.flexmodel.domain.model.flow.processor;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.SQLiteTestResource;
import tech.wetech.flexmodel.domain.model.flow.dto.param.CreateFlowParam;
import tech.wetech.flexmodel.domain.model.flow.dto.param.DeployFlowParam;
import tech.wetech.flexmodel.domain.model.flow.dto.param.GetFlowModuleParam;
import tech.wetech.flexmodel.domain.model.flow.dto.param.UpdateFlowParam;
import tech.wetech.flexmodel.domain.model.flow.dto.result.CreateFlowResult;
import tech.wetech.flexmodel.domain.model.flow.dto.result.DeployFlowResult;
import tech.wetech.flexmodel.domain.model.flow.dto.result.FlowModuleResult;
import tech.wetech.flexmodel.domain.model.flow.dto.result.UpdateFlowResult;
import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.util.EntityBuilder;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
@Slf4j
public class DefinitionProcessorTest {

  @Inject
  DefinitionProcessor definitionProcessor;

  @Test
  public void createTest() {
    CreateFlowParam createFlowParam = EntityBuilder.buildCreateFlowParam();
    CreateFlowResult createFlowResult = definitionProcessor.create(createFlowParam);
    log.info("createFlow.||createFlowResult={}", createFlowResult);
    Assertions.assertEquals(createFlowResult.getErrCode(), ErrorEnum.SUCCESS.getErrNo());
  }

  @Test
  public void updateTest() {
    CreateFlowParam createFlowParam = EntityBuilder.buildCreateFlowParam();
    UpdateFlowParam updateFlowParam = EntityBuilder.buildUpdateFlowParam();

    CreateFlowResult createFlowResult = definitionProcessor.create(createFlowParam);
    updateFlowParam.setFlowModuleId(createFlowResult.getFlowModuleId());
    UpdateFlowResult updateFlowResult = definitionProcessor.update(updateFlowParam);
    log.info("updateFlow.||result={}", updateFlowParam);
    Assertions.assertEquals(updateFlowResult.getErrCode(), ErrorEnum.SUCCESS.getErrNo());
  }

  @Test
  public void deployTest() {
    CreateFlowParam createFlowParam = EntityBuilder.buildCreateFlowParam();
    UpdateFlowParam updateFlowParam = EntityBuilder.buildUpdateFlowParam();
    DeployFlowParam deployFlowParam = EntityBuilder.buildDeployFlowParm();

    CreateFlowResult createFlowResult = definitionProcessor.create(createFlowParam);
    updateFlowParam.setFlowModuleId(createFlowResult.getFlowModuleId());
    UpdateFlowResult updateFlowResult = definitionProcessor.update(updateFlowParam);
    Assertions.assertEquals(updateFlowResult.getErrCode(), ErrorEnum.SUCCESS.getErrNo());
    deployFlowParam.setFlowModuleId(createFlowResult.getFlowModuleId());
    DeployFlowResult deployFlowResult = definitionProcessor.deploy(deployFlowParam);
    log.info("deployFlowTest.||deployFlowResult={}", deployFlowResult);
    Assertions.assertEquals(deployFlowResult.getErrCode(), ErrorEnum.SUCCESS.getErrNo());
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
    Assertions.assertEquals(updateFlowResult.getErrCode(), ErrorEnum.SUCCESS.getErrNo());

    flowModuleParam.setFlowModuleId(updateFlowParam.getFlowModuleId());
    FlowModuleResult flowModuleResultByFlowModuleId = definitionProcessor.getFlowModule(flowModuleParam);
    Assertions.assertEquals(flowModuleResultByFlowModuleId.getFlowModuleId(), createFlowResult.getFlowModuleId());

    deployFlowParam.setFlowModuleId(createFlowResult.getFlowModuleId());
    DeployFlowResult deployFlowResult = definitionProcessor.deploy(deployFlowParam);
    flowModuleParam.setFlowDeployId(deployFlowResult.getFlowDeployId());
    flowModuleParam.setFlowModuleId(null);
    FlowModuleResult flowModuleResultByDeployId = definitionProcessor.getFlowModule(flowModuleParam);
    Assertions.assertEquals(flowModuleResultByDeployId.getFlowModel(), updateFlowParam.getFlowModel());
  }

}
