package tech.wetech.flexmodel.domain.model.flow.executor;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.codegen.entity.InstanceData;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;
import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.shared.common.InstanceDataType;
import tech.wetech.flexmodel.domain.model.flow.shared.common.NodeInstanceStatus;
import tech.wetech.flexmodel.domain.model.flow.shared.common.RuntimeContext;
import tech.wetech.flexmodel.domain.model.flow.shared.util.GroovyUtil;
import tech.wetech.flexmodel.domain.model.flow.shared.util.JavaScriptUtil;
import tech.wetech.flexmodel.shared.utils.JsonUtils;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动任务执行器
 *
 * @author cjbi
 */
@Singleton
public class ServiceTaskExecutor extends ElementExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTaskExecutor.class);

  @Override
  protected void doExecute(RuntimeContext runtimeContext) throws ProcessException {
    NodeInstanceBO nodeInstance = runtimeContext.getCurrentNodeInstance();

    // 检查nodeInstance是否为空
    if (nodeInstance == null) {
      LOGGER.error("doExecute: nodeInstance is null");
      throw new ProcessException(ErrorEnum.MISSING_DATA, "当前节点实例为空");
    }

    // 检查节点状态，避免重复执行
    if (nodeInstance.getStatus() == NodeInstanceStatus.COMPLETED) {
      LOGGER.warn("doExecute: nodeInstance is already completed.||nodeInstanceId={}||nodeKey={}",
        nodeInstance.getNodeInstanceId(), nodeInstance.getNodeKey());
      return;
    }
    // 获取子类型
    Object subTypeObj = nodeInstance.get("subType");
    if (subTypeObj == null) {
      LOGGER.error("doExecute: subType is null.||nodeInstanceId={}||nodeKey={}",
        nodeInstance.getNodeInstanceId(), nodeInstance.getNodeKey());
      throw new ProcessException(ErrorEnum.MISSING_DATA, "服务任务缺少subType属性");
    }

    String subType = subTypeObj.toString();
    LOGGER.info("doExecute: executing service task.||nodeInstanceId={}||nodeKey={}||subType={}",
      nodeInstance.getNodeInstanceId(), nodeInstance.getNodeKey(), subType);

    try {
      // 获取脚本内容
      Object scriptObj = nodeInstance.get("script");
      if (scriptObj == null) {
        LOGGER.error("doExecute: script is null.||nodeInstanceId={}||nodeKey={}||subType={}",
          nodeInstance.getNodeInstanceId(), nodeInstance.getNodeKey(), subType);
        throw new ProcessException(ErrorEnum.MISSING_DATA, "服务任务缺少script属性");
      }

      String script = scriptObj.toString();
      if (StringUtils.isBlank(script)) {
        LOGGER.warn("doExecute: script is empty.||nodeInstanceId={}||nodeKey={}||subType={}",
          nodeInstance.getNodeInstanceId(), nodeInstance.getNodeKey(), subType);
        throw new ProcessException(ErrorEnum.MISSING_DATA, "服务任务脚本内容为空");
      }

      // 执行脚本
      Object result = executeScript(subType, script, runtimeContext.getInstanceDataMap());

      // 处理执行结果
      handleExecutionResult(nodeInstance, result, runtimeContext);

      // 更新节点状态
      nodeInstance.setStatus(NodeInstanceStatus.COMPLETED);
      runtimeContext.getNodeInstanceList().add(nodeInstance);

      LOGGER.info("doExecute: service task completed successfully.||nodeInstanceId={}||nodeKey={}||subType={}||result={}",
        nodeInstance.getNodeInstanceId(), nodeInstance.getNodeKey(), subType, result);

    } catch (ProcessException pe) {
      LOGGER.error("doExecute: ProcessException occurred.||nodeInstanceId={}||nodeKey={}||subType={}",
        nodeInstance.getNodeInstanceId(), nodeInstance.getNodeKey(), subType, pe);
      throw pe;
    } catch (Exception e) {
      LOGGER.error("doExecute: unexpected exception occurred.||nodeInstanceId={}||nodeKey={}||subType={}",
        nodeInstance.getNodeInstanceId(), nodeInstance.getNodeKey(), subType, e);
      throw new ProcessException(ErrorEnum.SERVICE_TASK_EXECUTION_FAILED.getErrNo(),
        "服务任务执行失败: " + e.getMessage());
    }
  }

  private String saveInstanceData(RuntimeContext runtimeContext) {
    Map<String, Object> instanceDataMap = runtimeContext.getInstanceDataMap();
    LOGGER.debug("saveInstanceData: saving instance data.||runtimeContext={}", runtimeContext);
    if (instanceDataMap == null || instanceDataMap.isEmpty()) {
      return "";
    }
    String instanceDataId = genId();
    InstanceData instanceData = new InstanceData();
    instanceData.setFlowInstanceId(runtimeContext.getFlowInstanceId());
    instanceData.setNodeInstanceId(runtimeContext.getCurrentNodeInstance().getNodeInstanceId());
    instanceData.setFlowDeployId(runtimeContext.getFlowDeployId());
    instanceData.setFlowModuleId(runtimeContext.getFlowModuleId());
    instanceData.setNodeKey(runtimeContext.getCurrentNodeModel().getKey());
    instanceData.setInstanceData(JsonUtils.getInstance().stringify(instanceDataMap));
    instanceData.setInstanceDataId(instanceDataId);
    instanceData.setType(InstanceDataType.EXECUTE);
    instanceData.setCreateTime(LocalDateTime.now());
    instanceData.setCaller(runtimeContext.getCaller());
    instanceData.setTenant(runtimeContext.getTenant());
    instanceDataRepository.insert(instanceData);
    return instanceDataId;
  }

  /**
   * 执行脚本
   */
  private Object executeScript(String subType, String script, Map<String, Object> contextData) throws Exception {
    switch (subType.toLowerCase()) {
      case "groovy" -> {
        LOGGER.debug("executeScript: executing Groovy script.||script={}", script);
        return GroovyUtil.execute(script, contextData);
      }
      case "js" -> {
        LOGGER.debug("executeScript: executing JavaScript script.||script={}", script);
        return JavaScriptUtil.executeScript(script, contextData);
      }
      default -> {
        LOGGER.error("executeScript: unsupported subType.||subType={}", subType);
        throw new ProcessException(ErrorEnum.UNSUPPORTED_ELEMENT_TYPE, "不支持的子类型：" + subType);
      }
    }
  }

  /**
   * 处理执行结果
   */
  private void handleExecutionResult(NodeInstanceBO nodeInstance, Object result, RuntimeContext runtimeContext) {
    if (result != null) {
      // 将执行结果存储到runtimeContext.instanceDataMap中
      Map<String, Object> instanceDataMap = runtimeContext.getInstanceDataMap();
      if (instanceDataMap == null) {
        instanceDataMap = new HashMap<>();
        runtimeContext.setInstanceDataMap(instanceDataMap);
      }

      // 存储执行结果
      instanceDataMap.put("executionResult", result);

      // 如果结果是Map类型，将其合并到实例数据中
      if (result instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        instanceDataMap.putAll(resultMap);
        LOGGER.debug("handleExecutionResult: merged result map to instance data.||nodeInstanceId={}||resultSize={}",
          nodeInstance.getNodeInstanceId(), resultMap.size());
      }
      String instanceDataId = saveInstanceData(runtimeContext);
      nodeInstance.setInstanceDataId(instanceDataId);
      runtimeContext.setInstanceDataId(instanceDataId);
    }
  }

}
