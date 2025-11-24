package tech.wetech.flexmodel.domain.model.flow.executor;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.codegen.entity.InstanceData;
import tech.wetech.flexmodel.domain.model.connect.SessionDatasource;
import tech.wetech.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;
import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import tech.wetech.flexmodel.domain.model.flow.shared.common.InstanceDataType;
import tech.wetech.flexmodel.domain.model.flow.shared.common.NodeInstanceStatus;
import tech.wetech.flexmodel.domain.model.flow.shared.common.RuntimeContext;
import tech.wetech.flexmodel.domain.model.flow.shared.util.GroovyUtil;
import tech.wetech.flexmodel.domain.model.flow.shared.util.JavaScriptUtil;
import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.shared.utils.JsonUtils;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 自动任务执行器
 *
 * @author cjbi
 */
@Singleton
public class ServiceTaskExecutor extends ElementExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTaskExecutor.class);

  @Inject
  SessionFactory sessionFactory;

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
      Object result;
      // CRUD 操作不需要 script 参数
      if (subType.equalsIgnoreCase("insert_record") ||
          subType.equalsIgnoreCase("update_record") ||
          subType.equalsIgnoreCase("delete_record") ||
          subType.equalsIgnoreCase("query_record")) {
        // 执行 CRUD 操作
        result = executeAction(subType, null, nodeInstance, runtimeContext.getInstanceDataMap());
      } else {
        // 获取脚本内容（用于 groovy、js 等脚本类型）
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
        result = executeAction(subType, script, nodeInstance, runtimeContext.getInstanceDataMap());
      }

      // 处理执行结果
      handleExecutionResult(nodeInstance, result, runtimeContext);

      // 更新节点状态
      nodeInstance.setStatus(NodeInstanceStatus.COMPLETED);
      runtimeContext.getNodeInstanceList().add(nodeInstance);

      LOGGER.info(
        "doExecute: service task completed successfully.||nodeInstanceId={}||nodeKey={}||subType={}||result={}",
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
    instanceData.setTenantId(runtimeContext.getTenantId());
    instanceDataRepository.insert(instanceData);
    return instanceDataId;
  }

  /**
   * 执行脚本或CRUD操作
   */
  private Object executeAction(String subType, String script, NodeInstanceBO nodeInstance, Map<String, Object> contextData) throws Exception {
    switch (subType.toLowerCase()) {
      case "script" -> {
        LOGGER.debug("executeScript: executing JavaScript script.||script={}", script);
        return JavaScriptUtil.execute(script, contextData);
      }
      case "sql" -> {
        LOGGER.debug("executeScript: executing SQL script.||script={}", script);
        return executeSqlScript(nodeInstance, contextData);
      }
      case "insert_record" -> {
        return executeInsertRecord(nodeInstance, contextData);
      }
      case "update_record" -> {
        return executeUpdateRecord(nodeInstance, contextData);
      }
      case "delete_record" -> {
        return executeDeleteRecord(nodeInstance, contextData);
      }
      case "query_record" -> {
        return executeQueryRecord(nodeInstance, contextData);
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
      String resultPath = getOptionalProperty(nodeInstance, "resultPath");
      // 将执行结果存储到runtimeContext.instanceDataMap中
      Map<String, Object> instanceDataMap = runtimeContext.getInstanceDataMap();
      if (instanceDataMap == null) {
        instanceDataMap = new HashMap<>();
        runtimeContext.setInstanceDataMap(instanceDataMap);
      }

      // 将结果存储到指定路径
      if (StringUtils.isNotBlank(resultPath)) {
        setResultPath(runtimeContext.getInstanceDataMap(), resultPath, result);
      } else if (result instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        instanceDataMap.putAll(resultMap);
        LOGGER.debug("handleExecutionResult: merged result map to instance data.||nodeInstanceId={}||resultSize={}",
          nodeInstance.getNodeInstanceId(), resultMap.size());
      } else {
        // 存储执行结果
        instanceDataMap.put("executionResult", result);
      }

      String instanceDataId = saveInstanceData(runtimeContext);
      nodeInstance.setInstanceDataId(instanceDataId);
      runtimeContext.setInstanceDataId(instanceDataId);
    }
  }

  /**
   * 执行新增记录操作
   */
  private Object executeInsertRecord(NodeInstanceBO nodeInstance, Map<String, Object> contextData) throws Exception {
    String datasourceName = getRequiredProperty(nodeInstance, "datasourceName");
    String modelName = getRequiredProperty(nodeInstance, "modelName");
    Object dataMappingObj = nodeInstance.get("dataMapping");
    String inputPath = getOptionalProperty(nodeInstance, "inputPath");
    LOGGER.debug("executeInsertRecord: datasource={}, model={}, inputPath={}",
      datasourceName, modelName, inputPath);

    int affectedRows = 0;

    try (Session session = sessionFactory.createSession(datasourceName)) {
      // 获取输入数据
      Object inputData = getInputData(inputPath, contextData);

      if (inputData instanceof List<?> dataList) {
        // 批量插入
        for (Object item : dataList) {
          Map<String, Object> recordData = applyDataMapping(dataMappingObj, (Map<String, Object>) item);
          affectedRows += session.dsl().insertInto(modelName).values(recordData).execute();
        }
      } else if (inputData instanceof Map) {
        // 单条插入
        Map<String, Object> recordData = applyDataMapping(dataMappingObj, (Map<String, Object>) inputData);
        affectedRows += session.dsl().insertInto(modelName).values(recordData).execute();
      } else {
        // 没有 inputPath，直接使用 dataMapping
        Map<String, Object> recordData = applyDataMapping(dataMappingObj, contextData);
        affectedRows += session.dsl().insertInto(modelName).values(recordData).execute();
      }
    }

    LOGGER.info("executeInsertRecord: completed, affectedRows={}", affectedRows);
    return affectedRows;
  }

  /**
   * 执行更新记录操作
   */
  private Object executeUpdateRecord(NodeInstanceBO nodeInstance, Map<String, Object> contextData) {
    String datasourceName = getRequiredProperty(nodeInstance, "datasourceName");
    String modelName = getRequiredProperty(nodeInstance, "modelName");
    Object dataMappingObj = nodeInstance.get("dataMapping");
    String inputPath = getOptionalProperty(nodeInstance, "inputPath");
    String filter = getOptionalProperty(nodeInstance, "filter");

    LOGGER.debug("executeUpdateRecord: datasource={}, model={}, filter={}, inputPath={}",
      datasourceName, modelName, filter, inputPath);

    int affectedRows = 0;

    try (Session session = sessionFactory.createSession(datasourceName)) {
      // 获取输入数据
      Object inputData = getInputData(inputPath, contextData);

      if (inputData instanceof List<?> dataList) {
        // 批量更新
        for (Object item : dataList) {
          Map<String, Object> recordData = applyDataMapping(dataMappingObj, (Map<String, Object>) item);
          String processedFilter = StringUtils.simpleRenderTemplate(filter, contextData);

          var updateBuilder = session.dsl().update(modelName).values(recordData);
          if (StringUtils.isNotBlank(processedFilter)) {
            updateBuilder.where(processedFilter);
          }
          affectedRows += updateBuilder.execute();
        }
      } else if (inputData instanceof Map) {
        // 单条更新
        Map<String, Object> recordData = applyDataMapping(dataMappingObj, (Map<String, Object>) inputData);
        String processedFilter = StringUtils.simpleRenderTemplate(filter, contextData);

        var updateBuilder = session.dsl().update(modelName).values(recordData);
        if (StringUtils.isNotBlank(processedFilter)) {
          updateBuilder.where(processedFilter);
        }
        affectedRows += updateBuilder.execute();
      } else {
        // 没有 inputPath，直接使用 dataMapping 和 filter
        Map<String, Object> recordData = applyDataMapping(dataMappingObj, contextData);
        String processedFilter = StringUtils.simpleRenderTemplate(filter, contextData);

        var updateBuilder = session.dsl().update(modelName).values(recordData);
        if (StringUtils.isNotBlank(processedFilter)) {
          updateBuilder.where(processedFilter);
        }
        affectedRows += updateBuilder.execute();
      }
    }

    LOGGER.info("executeUpdateRecord: completed, affectedRows={}", affectedRows);
    return affectedRows;
  }

  /**
   * 执行删除记录操作
   */
  private Object executeDeleteRecord(NodeInstanceBO nodeInstance, Map<String, Object> contextData) throws Exception {
    String datasourceName = getRequiredProperty(nodeInstance, "datasourceName");
    String modelName = getRequiredProperty(nodeInstance, "modelName");
    String filter = getOptionalProperty(nodeInstance, "filter");

    LOGGER.debug("executeDeleteRecord: datasource={}, model={}, filter={}",
      datasourceName, modelName, filter);

    try (Session session = sessionFactory.createSession(datasourceName)) {
      String processedFilter = StringUtils.simpleRenderTemplate(filter, contextData);

      var deleteBuilder = session.dsl().deleteFrom(modelName);
      if (StringUtils.isNotBlank(processedFilter)) {
        deleteBuilder.where(processedFilter);
      }
      return deleteBuilder.execute();
    }
  }

  /**
   * 执行查询记录操作
   */
  @SuppressWarnings("rawtypes")
  private Object executeQueryRecord(NodeInstanceBO nodeInstance, Map<String, Object> contextData) {
    String datasourceName = getRequiredProperty(nodeInstance, "datasourceName");
    String modelName = getRequiredProperty(nodeInstance, "modelName");
    String filter = getOptionalProperty(nodeInstance, "filter");
    String sortString = getOptionalProperty(nodeInstance, "sort");

    Integer page = null;
    Integer size = null;
    Object pageObj = nodeInstance.get("page");
    Object sizeObj = nodeInstance.get("size");

    if (pageObj != null && sizeObj != null) {
      page = Integer.parseInt(pageObj.toString());
      size = Integer.parseInt(sizeObj.toString());
    }

    LOGGER.debug("executeQueryRecord: datasource={}, model={}, filter={}, sort={}, page={}, size={}",
      datasourceName, modelName, filter, sortString, page, size);

    List<Map<String, Object>> records;

    try (Session session = sessionFactory.createSession(datasourceName)) {
      var queryBuilder = session.dsl().selectFrom(modelName);

      // 处理 filter
      if (StringUtils.isNotBlank(filter)) {
        String processedFilter = StringUtils.simpleRenderTemplate(filter, contextData);
        queryBuilder.where(processedFilter);
      }

      // 处理分页
      if (page != null) {
        queryBuilder.page(page, size);
      }

      // 处理排序
      if (StringUtils.isNotBlank(sortString)) {
        try {
          List<Map> orders = JsonUtils.getInstance().parseToList(sortString, Map.class);
          Query.OrderBy orderBy = new Query.OrderBy();
          for (Map order : orders) {
            orderBy.addOrder(order.get("field").toString(), Direction.fromString(order.get("direction").toString()));
          }
          queryBuilder.orderBy(orderBy);

        } catch (Exception e) {
          LOGGER.error("Invalid sort string: {}", sortString, e);
        }
      }
      records = queryBuilder.execute();
    }

    LOGGER.info("executeQueryRecord: completed, recordCount={}", records.size());
    return records;
  }

  /**
   * 应用 dataMapping 配置
   */
  private Map<String, Object> applyDataMapping(Object dataMappingObj, Map<String, Object> contextData) {
    Map<String, Object> result = new HashMap<>();

    if (dataMappingObj == null) {
      return result;
    }

    if (dataMappingObj instanceof List<?> mappingList) {
      for (Object item : mappingList) {
        if (item instanceof Map<?, ?> mapping) {
          String field = (String) mapping.get("field");
          Object valueTemplate = mapping.get("value");

          if (field != null && valueTemplate != null) {
            String valueStr = valueTemplate.toString();
            // 使用 StringUtils.simpleRenderTemplate 替换变量
            String processedValue = StringUtils.simpleRenderTemplate(valueStr, contextData);
            result.put(field, processedValue);
          }
        }
      }
    }

    return result;
  }

  /**
   * 从 contextData 中根据 inputPath 提取数据
   *
   * @param inputPath 输入路径
   * @param contextData 上下文数据
   * @return 提取的数据
   */
  private Object getInputData(String inputPath, Map<String, Object> contextData) {
    if (StringUtils.isBlank(inputPath)) {
      return null;
    }

    try {
      // 使用简化的路径解析提取数据
      return extractValueByPath(contextData, inputPath);
    } catch (Exception e) {
      LOGGER.error("Failed to extract data from inputPath: {}", inputPath, e);
      return null;
    }
  }

  /**
   * 将结果按照 resultPath 存入 contextData
   *
   * @param contextData 上下文数据
   * @param resultPath 结果路径
   * @param value 值
   */
  private void setResultPath(Map<String, Object> contextData, String resultPath, Object value) {
    if (StringUtils.isBlank(resultPath)) {
      contextData.put("executionResult", value);
      return;
    }

    String[] parts = resultPath.split("\\.");

    Map<String, Object> current = contextData;
    for (int i = 0; i < parts.length - 1; i++) {
      String part = parts[i];
      if (!current.containsKey(part)) {
        current.put(part, new HashMap<String, Object>());
      }
      Object next = current.get(part);
      if (next instanceof Map) {
        current = (Map<String, Object>) next;
      } else {
        // 如果路径中间节点不是 Map，无法继续
        LOGGER.warn("Cannot set resultPath {}, intermediate node {} is not a Map", resultPath, part);
        return;
      }
    }

    // 设置最后一个字段的值
    current.put(parts[parts.length - 1], value);
  }

  public Object extractValueByPath(Object data, String path) {
    if (data == null || path == null || path.isEmpty()) {
      return data;
    }

    String[] parts = path.split("\\.");
    Object current = data;

    for (String part : parts) {
      if (current == null) {
        return null;
      }

      if (current instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) current;
        current = map.get(part);
      } else if (current instanceof List) {
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) current;
        try {
          int index = Integer.parseInt(part);
          if (index >= 0 && index < list.size()) {
            current = list.get(index);
          } else {
            return null;
          }
        } catch (NumberFormatException e) {
          return null;
        }
      } else {
        return null;
      }
    }

    return current;
  }

  /**
   * 获取必填属性
   */
  private String getRequiredProperty(NodeInstanceBO nodeInstance, String propertyName) throws ProcessException {
    Object value = nodeInstance.get(propertyName);
    if (value == null) {
      throw new ProcessException(ErrorEnum.MISSING_DATA, "缺少必填属性: " + propertyName);
    }
    return value.toString();
  }

  /**
   * 获取可选属性
   */
  private String getOptionalProperty(NodeInstanceBO nodeInstance, String propertyName) {
    Object value = nodeInstance.get(propertyName);
    return value != null ? value.toString() : null;
  }

  /**
   * 执行SQL脚本
   */
  private Object executeSqlScript(NodeInstanceBO nodeInstance, Map<String, Object> contextData) {
    String datasourceName = getRequiredProperty(nodeInstance, "datasourceName");
    String script = getRequiredProperty(nodeInstance, "script");

    LOGGER.debug("executeSqlScript: datasource={}, script={}", datasourceName, script);

    // 使用StringUtils.simpleRenderTemplate处理SQL脚本中的变量替换
    String processedScript = StringUtils.simpleRenderTemplate(script, contextData);

    // 执行原生SQL查询
    long beginTime = System.currentTimeMillis();
    try (Session session = sessionFactory.createSession(datasourceName)) {
      return session.data().executeNativeStatement(processedScript, contextData);
    } finally {
      long endTime = System.currentTimeMillis() - beginTime;
      // 释放Session
      LOGGER.info("executeSqlScript: completed, executionTime={}ms", endTime);
    }
  }

}
