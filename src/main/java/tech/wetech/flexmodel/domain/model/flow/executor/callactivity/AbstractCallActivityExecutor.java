package tech.wetech.flexmodel.domain.model.flow.executor.callactivity;

import com.google.common.collect.Lists;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.InstanceData;
import tech.wetech.flexmodel.domain.model.flow.bo.DataTransferBO;
import tech.wetech.flexmodel.domain.model.flow.common.Constants;
import tech.wetech.flexmodel.domain.model.flow.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.common.InstanceDataType;
import tech.wetech.flexmodel.domain.model.flow.common.RuntimeContext;
import tech.wetech.flexmodel.domain.model.flow.config.BusinessConfig;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;
import tech.wetech.flexmodel.domain.model.flow.executor.ElementExecutor;
import tech.wetech.flexmodel.domain.model.flow.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.processor.RuntimeProcessor;
import tech.wetech.flexmodel.domain.model.flow.repository.FlowDeploymentRepository;
import tech.wetech.flexmodel.domain.model.flow.service.NodeInstanceService;
import tech.wetech.flexmodel.domain.model.flow.util.InstanceDataUtil;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

  protected List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> getCallActivityVariables(RuntimeContext runtimeContext) throws ProcessException {
    List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> callActivityInitData = InstanceDataUtil.getInstanceDataList(runtimeContext.getInstanceDataMap());
    List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataFromMainFlow = calculateCallActivityInParamFromMainFlow(runtimeContext);
    // merge data
    Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> callActivityInitDataMap = InstanceDataUtil.getInstanceDataMap(callActivityInitData);
    Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataFromMainFlowMap = InstanceDataUtil.getInstanceDataMap(instanceDataFromMainFlow);

    Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> allInstanceData = new HashMap<>();
    allInstanceData.putAll(callActivityInitDataMap);
    allInstanceData.putAll(instanceDataFromMainFlowMap);
    return InstanceDataUtil.getInstanceDataList(allInstanceData);
  }

  // main > sub
  protected List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> calculateCallActivityInParamFromMainFlow(RuntimeContext runtimeContext) throws ProcessException {
    FlowElement currentNodeModel = runtimeContext.getCurrentNodeModel();

    InstanceData instanceDataPO = instanceDataRepository.select(runtimeContext.getFlowInstanceId(), runtimeContext.getInstanceDataId());
    Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> mainInstanceDataMap = InstanceDataUtil.getInstanceDataMap(instanceDataPO.getInstanceData());

    return calculateCallActivityDataTransfer(currentNodeModel, mainInstanceDataMap,
      Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_IN_PARAM_TYPE,
      Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_IN_PARAM);
  }

  // sub > main
  protected List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> calculateCallActivityOutParamFromSubFlow(RuntimeContext runtimeContext, List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> subFlowData) throws ProcessException {
    FlowElement currentNodeModel = runtimeContext.getCurrentNodeModel();
    return calculateCallActivityDataTransfer(currentNodeModel, InstanceDataUtil.getInstanceDataMap(subFlowData),
      Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_OUT_PARAM_TYPE,
      Constants.ELEMENT_PROPERTIES.CALL_ACTIVITY_OUT_PARAM);
  }

  private List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> calculateCallActivityDataTransfer(FlowElement currentNodeModel, Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataMap, String callActivityParamType, String callActivityParam) throws ProcessException {
    // default FULL
    String callActivityInParamType = (String) currentNodeModel.getProperties().getOrDefault(callActivityParamType, Constants.CALL_ACTIVITY_PARAM_TYPE.FULL);
    if (callActivityInParamType.equals(Constants.CALL_ACTIVITY_PARAM_TYPE.NONE)) {
      return new ArrayList<>();
    }
    if (callActivityInParamType.equals(Constants.CALL_ACTIVITY_PARAM_TYPE.PART)) {
      List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> instanceDataList = Lists.newArrayList();
      String callActivityInParam = (String) currentNodeModel.getProperties().getOrDefault(callActivityParam, "");
      List<DataTransferBO> callActivityDataTransfers = tech.wetech.flexmodel.shared.utils.JsonUtils.getInstance().parseToList(callActivityInParam, DataTransferBO.class);
      for (DataTransferBO callActivityDataTransfer : callActivityDataTransfers) {
        if (Constants.CALL_ACTIVITY_DATA_TRANSFER_TYPE.SOURCE_TYPE_CONTEXT.equals(callActivityDataTransfer.getSourceType())) {
          tech.wetech.flexmodel.domain.model.flow.model.InstanceData sourceInstanceData = instanceDataMap.get(callActivityDataTransfer.getSourceKey());
          Object sourceValue = sourceInstanceData == null ? null : sourceInstanceData.getValue();
          tech.wetech.flexmodel.domain.model.flow.model.InstanceData instanceData = new tech.wetech.flexmodel.domain.model.flow.model.InstanceData(callActivityDataTransfer.getTargetKey(), sourceValue);
          instanceDataList.add(instanceData);
        } else if (Constants.CALL_ACTIVITY_DATA_TRANSFER_TYPE.SOURCE_TYPE_FIXED.equals(callActivityDataTransfer.getSourceType())) {
          tech.wetech.flexmodel.domain.model.flow.model.InstanceData instanceData = new tech.wetech.flexmodel.domain.model.flow.model.InstanceData(callActivityDataTransfer.getTargetKey(), callActivityDataTransfer.getSourceValue());
          instanceDataList.add(instanceData);
        } else {
          throw new ProcessException(ErrorEnum.MODEL_UNKNOWN_ELEMENT_VALUE);
        }
      }
      return instanceDataList;
    }
    if (callActivityInParamType.equals(Constants.CALL_ACTIVITY_PARAM_TYPE.FULL)) {
      return InstanceDataUtil.getInstanceDataList(instanceDataMap);
    }
    throw new ProcessException(ErrorEnum.MODEL_UNKNOWN_ELEMENT_VALUE);
  }

  protected InstanceData buildCallActivityEndInstanceData(String instanceDataId, RuntimeContext runtimeContext) {
    InstanceData instanceDataPO = JsonUtils.getInstance().convertValue(runtimeContext, InstanceData.class);
    instanceDataPO.setInstanceDataId(instanceDataId);
    instanceDataPO.setInstanceData(InstanceDataUtil.getInstanceDataListStr(runtimeContext.getInstanceDataMap()));
    instanceDataPO.setNodeInstanceId(runtimeContext.getCurrentNodeInstance().getNodeInstanceId());
    instanceDataPO.setNodeKey(runtimeContext.getCurrentNodeModel().getKey());
    instanceDataPO.setType(InstanceDataType.UPDATE);
    instanceDataPO.setCreateTime(LocalDateTime.now());
    return instanceDataPO;
  }
}
