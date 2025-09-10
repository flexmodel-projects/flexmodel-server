package tech.wetech.flexmodel.domain.model.flow.executor;

import com.google.common.collect.Lists;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.codegen.entity.InstanceData;
import tech.wetech.flexmodel.domain.model.flow.bo.NodeInstanceBO;
import tech.wetech.flexmodel.domain.model.flow.common.InstanceDataType;
import tech.wetech.flexmodel.domain.model.flow.common.NodeInstanceStatus;
import tech.wetech.flexmodel.domain.model.flow.common.RuntimeContext;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;
import tech.wetech.flexmodel.domain.model.flow.model.FlowElement;
import tech.wetech.flexmodel.domain.model.flow.spi.HookService;
import tech.wetech.flexmodel.domain.model.flow.util.FlowModelUtil;
import tech.wetech.flexmodel.domain.model.flow.util.InstanceDataUtil;
import tech.wetech.flexmodel.shared.utils.CollectionUtils;
import tech.wetech.flexmodel.shared.utils.JsonUtils;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> hookInfoValueMap = getHookInfoValueMap(runtimeContext.getFlowInstanceId(), hookInfoParam, runtimeContext.getCurrentNodeInstance().getNodeKey(), runtimeContext.getCurrentNodeInstance().getNodeInstanceId());
    LOGGER.info("doExecute getHookInfoValueMap.||hookInfoValueMap={}", hookInfoValueMap);
    if (hookInfoValueMap == null || hookInfoValueMap.isEmpty()) {
      LOGGER.warn("doExecute: hookInfoValueMap is empty.||flowInstanceId={}||hookInfoParam={}||nodeKey={}",
        runtimeContext.getFlowInstanceId(), hookInfoParam, flowElement.getKey());
      return;
    }

    // 4.merge data to current dataMap
    Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> dataMap = runtimeContext.getInstanceDataMap();
    dataMap.putAll(hookInfoValueMap);

    // 5.save data
    if (!dataMap.isEmpty()) {
      String instanceDataId = saveInstanceData(runtimeContext);
      runtimeContext.setInstanceDataId(instanceDataId);
    }
  }

  private Map<String, tech.wetech.flexmodel.domain.model.flow.model.InstanceData> getHookInfoValueMap(String flowInstanceId, String hookInfoParam, String nodeKey, String nodeInstanceId) {
    List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> dataList = Lists.newArrayList();
    for (HookService service : hookServices) {
      try {
        List<tech.wetech.flexmodel.domain.model.flow.model.InstanceData> list = service.invoke(flowInstanceId, hookInfoParam, nodeKey, nodeInstanceId);
        if (CollectionUtils.isEmpty(list)) {
          LOGGER.warn("hook service invoke result is empty, serviceName={}, flowInstanceId={}, hookInfoParam={}",
            service.getClass().getName(), flowInstanceId, hookInfoParam);
        }
        dataList.addAll(list);
      } catch (Exception e) {
        LOGGER.warn("hook service invoke fail, serviceName={}, flowInstanceId={}, hookInfoParam={}",
          service.getClass().getName(), flowInstanceId, hookInfoParam);
      }
    }
    return InstanceDataUtil.getInstanceDataMap(dataList);
  }

  private String saveInstanceData(RuntimeContext runtimeContext) {
    String instanceDataId = genId();
    InstanceData instanceData = buildHookInstanceData(instanceDataId, runtimeContext);
    instanceDataRepository.insert(instanceData);
    return instanceDataId;
  }

  private InstanceData buildHookInstanceData(String instanceDataId, RuntimeContext runtimeContext) {
    InstanceData instanceDataPO = JsonUtils.getInstance().convertValue(runtimeContext, InstanceData.class);
    instanceDataPO.setInstanceDataId(instanceDataId);
    instanceDataPO.setInstanceData(InstanceDataUtil.getInstanceDataListStr(runtimeContext.getInstanceDataMap()));
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
