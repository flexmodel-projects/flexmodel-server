package dev.flexmodel.domain.model.flow.executor;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.codegen.entity.InstanceData;
import dev.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.shared.common.InstanceDataType;
import dev.flexmodel.domain.model.flow.shared.common.NodeInstanceStatus;
import dev.flexmodel.domain.model.flow.shared.common.RuntimeContext;
import dev.flexmodel.domain.model.flow.shared.util.FlowModelUtil;
import dev.flexmodel.domain.model.flow.shared.util.InstanceDataUtil;
import dev.flexmodel.domain.model.flow.spi.HookService;
import dev.flexmodel.shared.utils.JsonUtils;
import dev.flexmodel.shared.utils.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ExclusiveGatewayExecutor extends ElementExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExclusiveGatewayExecutor.class);

  @Inject
  Instance<HookService> hookServiceInstance;

  private List<HookService> hookServices;

  /**
   * Update data map: invoke hook service to update data map
   * You can implement HookService and all implementations of 'HookService' will be executed.
   * Param: one of flowElement's properties
   */
  @Override
  protected void doExecute(RuntimeContext runtimeContext) throws ProcessException {
    // 1.get hook param
    FlowElement flowElement = runtimeContext.getCurrentNodeModel();
    String hookInfoParam = FlowModelUtil.getHookInfos(flowElement);

    // 2.ignore while properties is empty
    if (StringUtils.isBlank(hookInfoParam)) {
      return;
    }

    // 3.invoke hook and get data result
    Map<String, Object> hookInfoValueMap = getHookInfoValueMap(runtimeContext.getFlowInstanceId(), hookInfoParam, runtimeContext.getCurrentNodeInstance().getNodeKey(), runtimeContext.getCurrentNodeInstance().getNodeInstanceId());
    LOGGER.info("doExecute getHookInfoValueMap.||hookInfoValueMap={}", hookInfoValueMap);
    if (hookInfoValueMap == null || hookInfoValueMap.isEmpty()) {
      LOGGER.warn("doExecute: hookInfoValueMap is empty.||flowInstanceId={}||hookInfoParam={}||nodeKey={}",
        runtimeContext.getFlowInstanceId(), hookInfoParam, flowElement.getKey());
      return;
    }

    // 4.merge data to current dataMap
    Map<String, Object> dataMap = runtimeContext.getInstanceDataMap();
    dataMap.putAll(hookInfoValueMap);

    // 5.save data
    if (!dataMap.isEmpty()) {
      String instanceDataId = saveInstanceData(runtimeContext);
      runtimeContext.setInstanceDataId(instanceDataId);
    }
  }

  private Map<String, Object> getHookInfoValueMap(String flowInstanceId, String hookInfoParam, String nodeKey, String nodeInstanceId) {
    Map<String, Object> resultMap = new HashMap<>();
    for (HookService service : hookServices) {
      try {
        Map<String, Object> dataMap = service.invoke(flowInstanceId, hookInfoParam, nodeKey, nodeInstanceId);
        if (dataMap == null || dataMap.isEmpty()) {
          LOGGER.warn("hook service invoke result is empty, serviceName={}, flowInstanceId={}, hookInfoParam={}",
            service.getClass().getName(), flowInstanceId, hookInfoParam);
        } else {
          // 将Map合并到结果Map中
          resultMap.putAll(dataMap);
        }
      } catch (Exception e) {
        LOGGER.warn("hook service invoke fail, serviceName={}, flowInstanceId={}, hookInfoParam={}",
          service.getClass().getName(), flowInstanceId, hookInfoParam);
      }
    }
    return resultMap;
  }

  private String saveInstanceData(RuntimeContext runtimeContext) {
    String instanceDataId = genId();
    InstanceData instanceData = buildHookInstanceData(instanceDataId, runtimeContext);
    instanceDataRepository.insert(instanceData);
    return instanceDataId;
  }

  private InstanceData buildHookInstanceData(String instanceDataId, RuntimeContext runtimeContext) {
    InstanceData instanceDataPO = JsonUtils.getInstance().convertValue(runtimeContext, InstanceData.class);
    instanceDataPO.setProjectId(runtimeContext.getProjectId());
    instanceDataPO.setInstanceDataId(instanceDataId);
    instanceDataPO.setInstanceData(InstanceDataUtil.getInstanceDataStr(runtimeContext.getInstanceDataMap()));
    instanceDataPO.setNodeInstanceId(runtimeContext.getCurrentNodeInstance().getNodeInstanceId());
    instanceDataPO.setNodeKey(runtimeContext.getCurrentNodeModel().getKey());
    instanceDataPO.setType(InstanceDataType.HOOK);
    instanceDataPO.setCreateTime(LocalDateTime.now());
    return instanceDataPO;
  }

  @Override
  protected void postExecute(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO currentNodeInstance = runtimeContext.getCurrentNodeInstance();
    currentNodeInstance.setInstanceDataId(runtimeContext.getInstanceDataId());
    currentNodeInstance.setStatus(NodeInstanceStatus.COMPLETED);
    runtimeContext.getNodeInstanceList().add(currentNodeInstance);
  }

  /**
   * Calculate unique outgoing
   * Expression: one of flowElement's properties
   * Input: data map
   *
   * @return
   * @throws Exception
   */
  @Override
  protected RuntimeExecutor getExecuteExecutor(RuntimeContext runtimeContext) throws ProcessException {
    FlowElement nextNode = calculateNextNode(runtimeContext.getCurrentNodeModel(),
      runtimeContext.getFlowElementMap(), runtimeContext.getInstanceDataMap());

    runtimeContext.setCurrentNodeModel(nextNode);
    return executorFactoryInstance.get().getElementExecutor(nextNode);
  }

  @jakarta.annotation.PostConstruct
  public void init() {
    ensureHookService();
  }

  private void ensureHookService() {
    if (hookServices != null) {
      return;
    }

    // init hook services by CDI instance
    synchronized (ExclusiveGatewayExecutor.class) {
      if (hookServices != null) {
        return;
      }
      hookServices = new ArrayList<>();
      for (HookService service : hookServiceInstance) {
        hookServices.add(service);
      }
    }
  }
}
