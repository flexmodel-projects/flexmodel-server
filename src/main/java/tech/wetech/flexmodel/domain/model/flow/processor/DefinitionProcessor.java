package tech.wetech.flexmodel.domain.model.flow.processor;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.codegen.entity.FlowDefinition;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.domain.model.flow.dto.param.CreateFlowParam;
import tech.wetech.flexmodel.domain.model.flow.dto.param.DeployFlowParam;
import tech.wetech.flexmodel.domain.model.flow.dto.param.GetFlowModuleParam;
import tech.wetech.flexmodel.domain.model.flow.dto.param.UpdateFlowParam;
import tech.wetech.flexmodel.domain.model.flow.dto.result.*;
import tech.wetech.flexmodel.domain.model.flow.exception.DefinitionException;
import tech.wetech.flexmodel.domain.model.flow.exception.ParamException;
import tech.wetech.flexmodel.domain.model.flow.exception.TurboException;
import tech.wetech.flexmodel.domain.model.flow.plugin.IdGeneratorPlugin;
import tech.wetech.flexmodel.domain.model.flow.plugin.manager.PluginManager;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDefinitionRepository;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.shared.common.FlowDefinitionStatus;
import tech.wetech.flexmodel.domain.model.flow.shared.common.FlowDeploymentStatus;
import tech.wetech.flexmodel.domain.model.flow.shared.common.FlowModuleEnum;
import tech.wetech.flexmodel.domain.model.flow.shared.util.IdGenerator;
import tech.wetech.flexmodel.domain.model.flow.shared.util.StrongUuidGenerator;
import tech.wetech.flexmodel.domain.model.flow.validator.ModelValidator;
import tech.wetech.flexmodel.domain.model.flow.validator.ParamValidator;
import tech.wetech.flexmodel.shared.utils.JsonUtils;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.util.List;

@ApplicationScoped
public class DefinitionProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionProcessor.class);

  private static IdGenerator idGenerator;

  @Inject
  PluginManager pluginManager;

  @Inject
  ModelValidator modelValidator;

  @Inject
  FlowDefinitionRepository flowDefinitionRepository;

  @Inject
  FlowDeploymentRepository flowDeploymentRepository;

  @PostConstruct
  public void init() {
    List<IdGeneratorPlugin> idGeneratorPlugins = pluginManager.getPluginsFor(IdGeneratorPlugin.class);
    if (null == idGeneratorPlugins || idGeneratorPlugins.isEmpty()) {
      idGenerator = new StrongUuidGenerator();
    } else {
      idGenerator = idGeneratorPlugins.get(0).getIdGenerator();
    }
  }

  public CreateFlowResult create(CreateFlowParam createFlowParam) {
    CreateFlowResult createFlowResult = new CreateFlowResult();
    try {
      ParamValidator.validate(createFlowParam);

      FlowDefinition flowDefinitionPO = JsonUtils.getInstance().convertValue(createFlowParam, FlowDefinition.class);
      String flowModuleId = idGenerator.getNextId();
      flowDefinitionPO.setFlowModuleId(flowModuleId);
      flowDefinitionPO.setStatus(FlowDefinitionStatus.INIT);

      int rows = flowDefinitionRepository.insert(flowDefinitionPO);
      if (rows != 1) {
        LOGGER.warn("create flow failed: insert to db failed.||createFlowParam={}", createFlowParam);
        throw new DefinitionException(ErrorEnum.DEFINITION_INSERT_INVALID);
      }

      createFlowResult = JsonUtils.getInstance().convertValue(flowDefinitionPO, CreateFlowResult.class);
      fillCommonResult(createFlowResult, ErrorEnum.SUCCESS);
    } catch (TurboException te) {
      fillCommonResult(createFlowResult, te);
    }
    return createFlowResult;
  }

  public UpdateFlowResult update(UpdateFlowParam updateFlowParam) {
    UpdateFlowResult updateFlowResult = new UpdateFlowResult();
    try {
      ParamValidator.validate(updateFlowParam);

      FlowDefinition flowDefinitionPO = JsonUtils.getInstance().convertValue(updateFlowParam, FlowDefinition.class);
      flowDefinitionPO.setStatus(FlowDefinitionStatus.EDITING);

      int rows = flowDefinitionRepository.updateByModuleId(flowDefinitionPO);
      if (rows != 1) {
        LOGGER.warn("update flow failed: update to db failed.||updateFlowParam={}", updateFlowParam);
        throw new DefinitionException(ErrorEnum.DEFINITION_UPDATE_INVALID);
      }
      fillCommonResult(updateFlowResult, ErrorEnum.SUCCESS);
    } catch (TurboException te) {
      te.printStackTrace();
      fillCommonResult(updateFlowResult, te);
    }
    return updateFlowResult;
  }

  public DeployFlowResult deploy(DeployFlowParam deployFlowParam) {
    DeployFlowResult deployFlowResult = new DeployFlowResult();
    try {
      ParamValidator.validate(deployFlowParam);

      FlowDefinition flowDefinitionPO = flowDefinitionRepository.selectByModuleId(deployFlowParam.getFlowModuleId());
      if (null == flowDefinitionPO) {
        LOGGER.warn("deploy flow failed: flow is not exist.||deployFlowParam={}", deployFlowParam);
        throw new DefinitionException(ErrorEnum.FLOW_NOT_EXIST);
      }

      Integer status = flowDefinitionPO.getStatus();
      if (status != FlowDefinitionStatus.EDITING) {
        LOGGER.warn("deploy flow failed: flow is not editing status.||deployFlowParam={}", deployFlowParam);
        throw new DefinitionException(ErrorEnum.FLOW_NOT_EDITING);
      }

      String flowModel = flowDefinitionPO.getFlowModel();
      modelValidator.validate(flowModel, deployFlowParam);

      FlowDeployment flowDeploymentPO = JsonUtils.getInstance().convertValue(flowDefinitionPO, FlowDeployment.class);
      // fix primary key duplicated
      flowDeploymentPO.setId(null);
      String flowDeployId = idGenerator.getNextId();
      flowDeploymentPO.setFlowDeployId(flowDeployId);
      flowDeploymentPO.setStatus(FlowDeploymentStatus.DEPLOYED);

      int rows = flowDeploymentRepository.insert(flowDeploymentPO);
      if (rows != 1) {
        LOGGER.warn("deploy flow failed: insert to db failed.||deployFlowParam={}", deployFlowParam);
        throw new DefinitionException(ErrorEnum.DEFINITION_INSERT_INVALID);
      }
      deployFlowResult = JsonUtils.getInstance().convertValue(flowDeploymentPO, DeployFlowResult.class);
      fillCommonResult(deployFlowResult, ErrorEnum.SUCCESS);
    } catch (TurboException te) {
      fillCommonResult(deployFlowResult, te);
    }
    return deployFlowResult;
  }

  public FlowModuleResult getFlowModule(GetFlowModuleParam getFlowModuleParam) {
    FlowModuleResult flowModuleResult = new FlowModuleResult();
    try {
      ParamValidator.validate(getFlowModuleParam);
      String flowModuleId = getFlowModuleParam.getFlowModuleId();
      String flowDeployId = getFlowModuleParam.getFlowDeployId();
      if (StringUtils.isNotBlank(flowDeployId)) {
        flowModuleResult = getFlowModuleByFlowDeployId(flowDeployId);
      } else {
        flowModuleResult = getFlowModuleByFlowModuleId(flowModuleId);
      }
      fillCommonResult(flowModuleResult, ErrorEnum.SUCCESS);
    } catch (TurboException te) {
      fillCommonResult(flowModuleResult, te);
    }
    return flowModuleResult;
  }

  private FlowModuleResult getFlowModuleByFlowModuleId(String flowModuleId) throws ParamException {
    FlowDefinition flowDefinitionPO = flowDefinitionRepository.selectByModuleId(flowModuleId);
    if (flowDefinitionPO == null) {
      LOGGER.warn("getFlowModuleByFlowModuleId failed: can not find flowDefinitionPO.||flowModuleId={}", flowModuleId);
      throw new ParamException(ErrorEnum.PARAM_INVALID.getErrNo(), "flowDefinitionPO is not exist");
    }
    FlowModuleResult flowModuleResult = JsonUtils.getInstance().convertValue(flowDefinitionPO, FlowModuleResult.class);
    Integer status = FlowModuleEnum.getStatusByDefinitionStatus(flowDefinitionPO.getStatus());
    flowModuleResult.setStatus(status);
    LOGGER.info("getFlowModuleByFlowModuleId||flowModuleId={}||FlowModuleResult={}", flowModuleId, JsonUtils.getInstance().stringify(flowModuleResult));
    return flowModuleResult;
  }

  private FlowModuleResult getFlowModuleByFlowDeployId(String flowDeployId) throws ParamException {
    FlowDeployment flowDeploymentPO = flowDeploymentRepository.findByDeployId(flowDeployId);
    if (flowDeploymentPO == null) {
      LOGGER.warn("getFlowModuleByFlowDeployId failed: can not find flowDefinitionPO.||flowDeployId={}", flowDeployId);
      throw new ParamException(ErrorEnum.PARAM_INVALID.getErrNo(), "flowDefinitionPO is not exist");
    }
    FlowModuleResult flowModuleResult = JsonUtils.getInstance().convertValue(flowDeploymentPO, FlowModuleResult.class);
    Integer status = FlowModuleEnum.getStatusByDeploymentStatus(flowDeploymentPO.getStatus());
    flowModuleResult.setStatus(status);
    LOGGER.info("getFlowModuleByFlowDeployId||flowDeployId={}||response={}", flowDeployId, JsonUtils.getInstance().stringify(flowModuleResult));
    return flowModuleResult;
  }

  private void fillCommonResult(CommonResult commonResult, ErrorEnum errorEnum) {
    fillCommonResult(commonResult, errorEnum.getErrNo(), errorEnum.getErrMsg());
  }

  private void fillCommonResult(CommonResult commonResult, TurboException turboException) {
    fillCommonResult(commonResult, turboException.getErrNo(), turboException.getErrMsg());
  }

  private void fillCommonResult(CommonResult commonResult, int errNo, String errMsg) {
    commonResult.setErrCode(errNo);
    commonResult.setErrMsg(errMsg);
  }
}
