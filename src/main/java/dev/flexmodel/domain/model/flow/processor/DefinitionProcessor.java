package dev.flexmodel.domain.model.flow.processor;

import dev.flexmodel.domain.model.flow.dto.result.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.codegen.entity.FlowDefinition;
import dev.flexmodel.codegen.entity.FlowDeployment;
import dev.flexmodel.domain.model.flow.dto.param.CreateFlowParam;
import dev.flexmodel.domain.model.flow.dto.param.DeployFlowParam;
import dev.flexmodel.domain.model.flow.dto.param.GetFlowModuleParam;
import dev.flexmodel.domain.model.flow.dto.param.UpdateFlowParam;
import dev.flexmodel.domain.model.flow.dto.result.*;
import dev.flexmodel.domain.model.flow.exception.DefinitionException;
import dev.flexmodel.domain.model.flow.exception.ParamException;
import dev.flexmodel.domain.model.flow.exception.TurboException;
import dev.flexmodel.domain.model.flow.plugin.IdGeneratorPlugin;
import dev.flexmodel.domain.model.flow.plugin.manager.PluginManager;
import dev.flexmodel.domain.model.flow.repository.FlowDefinitionRepository;
import dev.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.domain.model.flow.shared.common.FlowDefinitionStatus;
import dev.flexmodel.domain.model.flow.shared.common.FlowDeploymentStatus;
import dev.flexmodel.domain.model.flow.shared.common.FlowModuleEnum;
import dev.flexmodel.domain.model.flow.shared.util.IdGenerator;
import dev.flexmodel.domain.model.flow.shared.util.StrongUuidGenerator;
import dev.flexmodel.domain.model.flow.validator.ModelValidator;
import dev.flexmodel.domain.model.flow.validator.ParamValidator;
import dev.flexmodel.shared.utils.JsonUtils;
import dev.flexmodel.shared.utils.StringUtils;

import java.time.LocalDateTime;
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


  public void delete(String projectId, String flowModuleId) {
    if (StringUtils.isBlank(flowModuleId)) {
      throw new ParamException(ErrorEnum.PARAM_INVALID.getErrNo(), "flowModuleId is null");
    }
    FlowDefinition flowDefinition = flowDefinitionRepository.selectByModuleId(projectId, flowModuleId);
    if (null == flowDefinition) {
      LOGGER.warn("delete flow failed: flow is not exist.||projectId={}, flowModuleId={}", projectId, flowModuleId);
      throw new DefinitionException(ErrorEnum.FLOW_NOT_EXIST);
    }
    flowDefinition.setIsDeleted(true);
    flowDefinition.setModifyTime(LocalDateTime.now());
    flowDefinitionRepository.updateByModuleId(flowDefinition);
  }

  public DeployFlowResult deploy(DeployFlowParam deployFlowParam) {
    DeployFlowResult deployFlowResult = new DeployFlowResult();
    try {
      ParamValidator.validate(deployFlowParam);

      FlowDefinition flowDefinitionPO = flowDefinitionRepository.selectByModuleId(deployFlowParam.getProjectId(), deployFlowParam.getFlowModuleId());
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
    String projectId = getFlowModuleParam.getProjectId();
    FlowModuleResult flowModuleResult = new FlowModuleResult();
    try {
      ParamValidator.validate(getFlowModuleParam);
      String flowModuleId = getFlowModuleParam.getFlowModuleId();
      String flowDeployId = getFlowModuleParam.getFlowDeployId();
      if (StringUtils.isNotBlank(flowDeployId)) {
        flowModuleResult = getFlowModuleByFlowDeployId(projectId, flowDeployId);
      } else {
        flowModuleResult = getFlowModuleByFlowModuleId(projectId, flowModuleId);
      }
      fillCommonResult(flowModuleResult, ErrorEnum.SUCCESS);
    } catch (TurboException te) {
      fillCommonResult(flowModuleResult, te);
    }
    return flowModuleResult;
  }

  private FlowModuleResult getFlowModuleByFlowModuleId(String projectId, String flowModuleId) throws ParamException {
    FlowDefinition flowDefinitionPO = flowDefinitionRepository.selectByModuleId(projectId, flowModuleId);
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

  private FlowModuleResult getFlowModuleByFlowDeployId(String projectId, String flowDeployId) throws ParamException {
    FlowDeployment flowDeploymentPO = flowDeploymentRepository.findByDeployId(projectId, flowDeployId);
    if (flowDeploymentPO == null) {
      LOGGER.warn("getFlowModuleByFlowDeployId failed: can not find flowDeploymentPO.||projectId={}, flowDeployId={}", projectId, flowDeployId);
      throw new ParamException(ErrorEnum.PARAM_INVALID.getErrNo(), "flowDeploymentPO is not exist");
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
