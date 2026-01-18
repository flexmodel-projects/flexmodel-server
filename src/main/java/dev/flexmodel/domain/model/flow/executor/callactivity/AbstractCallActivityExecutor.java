package dev.flexmodel.domain.model.flow.executor.callactivity;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.InstanceData;
import dev.flexmodel.domain.model.flow.config.BusinessConfig;
import dev.flexmodel.domain.model.flow.dto.bo.DataTransferBO;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.executor.ElementExecutor;
import dev.flexmodel.domain.model.flow.processor.RuntimeProcessor;
import dev.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import dev.flexmodel.domain.model.flow.service.NodeInstanceService;
import dev.flexmodel.domain.model.flow.shared.common.Constants;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.domain.model.flow.shared.common.InstanceDataType;
import dev.flexmodel.domain.model.flow.shared.common.RuntimeContext;
import dev.flexmodel.domain.model.flow.shared.util.InstanceDataUtil;
import dev.flexmodel.shared.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some common CallActivity methods
 */
public abstract class AbstractCallActivityExecutor extends ElementExecutor {

  @Inject
  protected Instance<RuntimeProcessor> runtimeProcessorInstance;

  @Inject
  protected FlowDeploymentRepository flowDeploymentRepository;

  @Inject
  protected NodeInstanceService nodeInstanceService;

  @Inject
  protected BusinessConfig businessConfig;

  protected Map<String, Object> getCallActivityVariables(RuntimeContext runtimeContext) throws ProcessException {
    Map<String, Object> callActivityInitData = runtimeContext.getInstanceDataMap();
    Map<String, Object> instanceDataFromMainFlow = calculateCallActivityInParamFromMainFlow(runtimeContext);
    // merge data
    Map<String, Object> allInstanceData = new HashMap<>();
    if (callActivityInitData != null) {
      allInstanceData.putAll(callActivityInitData);
    }
    if (instanceDataFromMainFlow != null) {
      allInstanceData.putAll(instanceDataFromMainFlow);
    }
    return allInstanceData;
  }

  // main > sub
  protected Map<String, Object> calculateCallActivityInParamFromMainFlow(RuntimeContext runtimeContext) throws ProcessException {
    FlowElement currentNodeModel = runtimeContext.getCurrentNodeModel();

    InstanceData instanceDataPO = instanceDataRepository.select(runtimeContext.getProjectId(), runtimeContext.getFlowInstanceId(), runtimeContext.getInstanceDataId());
    Map<String, Object> mainInstanceDataMap = InstanceDataUtil.getInstanceDataMap(instanceDataPO.getInstanceData());

    return calculateCallActivityDataTransfer(currentNodeModel, mainInstanceDataMap,
      Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_IN_PARAM_TYPE,
      Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_IN_PARAM);
  }

  // sub > main
  protected Map<String, Object> calculateCallActivityOutParamFromSubFlow(RuntimeContext runtimeContext, Map<String, Object> subFlowData) throws ProcessException {
    FlowElement currentNodeModel = runtimeContext.getCurrentNodeModel();
    return calculateCallActivityDataTransfer(currentNodeModel, subFlowData,
      Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_OUT_PARAM_TYPE,
      Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_OUT_PARAM);
  }

  private Map<String, Object> calculateCallActivityDataTransfer(FlowElement currentNodeModel, Map<String, Object> instanceDataMap, String callActivityParamType, String callActivityParam) throws ProcessException {
    // default FULL
    String callActivityInParamType = (String) currentNodeModel.getProperties().getOrDefault(callActivityParamType, Constants.CALL_ACTIVITY_PARAM_TYPE.FULL);
    if (callActivityInParamType.equals(Constants.CALL_ACTIVITY_PARAM_TYPE.NONE)) {
      return new HashMap<>();
    }
    if (callActivityInParamType.equals(Constants.CALL_ACTIVITY_PARAM_TYPE.PART)) {
      Map<String, Object> resultDataMap = new HashMap<>();
      String callActivityInParam = (String) currentNodeModel.getProperties().getOrDefault(callActivityParam, "");
      List<DataTransferBO> callActivityDataTransfers = JsonUtils.getInstance().parseToList(callActivityInParam, DataTransferBO.class);
      for (DataTransferBO callActivityDataTransfer : callActivityDataTransfers) {
        if (Constants.CALL_ACTIVITY_DATA_TRANSFER_TYPE.SOURCE_TYPE_CONTEXT.equals(callActivityDataTransfer.getSourceType())) {
          Object sourceValue = instanceDataMap.get(callActivityDataTransfer.getSourceKey());
          resultDataMap.put(callActivityDataTransfer.getTargetKey(), sourceValue);
        } else if (Constants.CALL_ACTIVITY_DATA_TRANSFER_TYPE.SOURCE_TYPE_FIXED.equals(callActivityDataTransfer.getSourceType())) {
          resultDataMap.put(callActivityDataTransfer.getTargetKey(), callActivityDataTransfer.getSourceValue());
        } else {
          throw new ProcessException(ErrorEnum.MODEL_UNKNOWN_ELEMENT_VALUE);
        }
      }
      return resultDataMap;
    }
    if (callActivityInParamType.equals(Constants.CALL_ACTIVITY_PARAM_TYPE.FULL)) {
      return instanceDataMap;
    }
    throw new ProcessException(ErrorEnum.MODEL_UNKNOWN_ELEMENT_VALUE);
  }

  protected InstanceData buildCallActivityEndInstanceData(String instanceDataId, RuntimeContext runtimeContext) {
    InstanceData instanceDataPO = JsonUtils.getInstance().convertValue(runtimeContext, InstanceData.class);
    instanceDataPO.setInstanceDataId(instanceDataId);
    instanceDataPO.setInstanceData(InstanceDataUtil.getInstanceDataStr(runtimeContext.getInstanceDataMap()));
    instanceDataPO.setNodeInstanceId(runtimeContext.getCurrentNodeInstance().getNodeInstanceId());
    instanceDataPO.setNodeKey(runtimeContext.getCurrentNodeModel().getKey());
    instanceDataPO.setType(InstanceDataType.UPDATE);
    instanceDataPO.setCreateTime(LocalDateTime.now());
    instanceDataPO.setProjectId(runtimeContext.getProjectId());
    return instanceDataPO;
  }
}
