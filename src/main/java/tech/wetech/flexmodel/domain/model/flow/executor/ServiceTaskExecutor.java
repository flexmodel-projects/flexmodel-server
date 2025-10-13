package tech.wetech.flexmodel.domain.model.flow.executor;

import jakarta.inject.Inject;
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
import tech.wetech.flexmodel.jsonlogic.JsonPathUtils;
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
    instanceData.setTenantId(runtimeContext.getTenant());
    instanceDataRepository.insert(instanceData);
    return instanceDataId;
  }

  /**
   * 执行脚本或CRUD操作
   */
  private Object executeAction(String subType, String script, NodeInstanceBO nodeInstance, Map<String, Object> contextData) throws Exception {
    switch (subType.toLowerCase()) {
      case "groovy" -> {
        LOGGER.debug("executeScript: executing Groovy script.||script={}", script);
        return GroovyUtil.execute(script, contextData);
      }
      case "js" -> {
        LOGGER.debug("executeScript: executing JavaScript script.||script={}", script);
        return JavaScriptUtil.executeScript(script, contextData);
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

  /**
   * 执行新增记录操作
   */
  private Object executeInsertRecord(NodeInstanceBO nodeInstance, Map<String, Object> contextData) throws Exception {
    String datasourceName = getRequiredProperty(nodeInstance, "datasourceName");
    String modelName = getRequiredProperty(nodeInstance, "modelName");
    Object dataMappingObj = nodeInstance.get("dataMapping");
    String inputPath = getOptionalProperty(nodeInstance, "inputPath");
    String resultPath = getOptionalProperty(nodeInstance, "resultPath");

    LOGGER.debug("executeInsertRecord: datasource={}, model={}, inputPath={}, resultPath={}",
        datasourceName, modelName, inputPath, resultPath);

    int affectedRows = 0;

    try (Session session = sessionFactory.createSession(datasourceName)) {
      // 获取输入数据
      Object inputData = getInputData(inputPath, contextData);

      if (inputData instanceof List) {
        // 批量插入
        List<?> dataList = (List<?>) inputData;
        for (Object item : dataList) {
          Map<String, Object> recordData = applyDataMapping(dataMappingObj, (Map<String, Object>) item);
          session.dsl().insertInto(modelName).values(recordData).execute();
          affectedRows++;
        }
      } else if (inputData instanceof Map) {
        // 单条插入
        Map<String, Object> recordData = applyDataMapping(dataMappingObj, (Map<String, Object>) inputData);
        session.dsl().insertInto(modelName).values(recordData).execute();
        affectedRows = 1;
      } else {
        // 没有 inputPath，直接使用 dataMapping
        Map<String, Object> recordData = applyDataMapping(dataMappingObj, contextData);
        session.dsl().insertInto(modelName).values(recordData).execute();
        affectedRows = 1;
      }
    }

    // 将结果存储到指定路径
    if (StringUtils.isNotBlank(resultPath)) {
      setResultPath(contextData, resultPath, affectedRows);
    }

    LOGGER.info("executeInsertRecord: completed, affectedRows={}", affectedRows);
    return Map.of("affectedRows", affectedRows);
  }

  /**
   * 执行更新记录操作
   */
  private Object executeUpdateRecord(NodeInstanceBO nodeInstance, Map<String, Object> contextData) throws Exception {
    String datasourceName = getRequiredProperty(nodeInstance, "datasourceName");
    String modelName = getRequiredProperty(nodeInstance, "modelName");
    Object dataMappingObj = nodeInstance.get("dataMapping");
    String inputPath = getOptionalProperty(nodeInstance, "inputPath");
    String filter = getOptionalProperty(nodeInstance, "filter");
    String resultPath = getOptionalProperty(nodeInstance, "resultPath");

    LOGGER.debug("executeUpdateRecord: datasource={}, model={}, filter={}, inputPath={}, resultPath={}",
        datasourceName, modelName, filter, inputPath, resultPath);

    int affectedRows = 0;

    try (Session session = sessionFactory.createSession(datasourceName)) {
      // 获取输入数据
      Object inputData = getInputData(inputPath, contextData);

      if (inputData instanceof List) {
        // 批量更新
        List<?> dataList = (List<?>) inputData;
        for (Object item : dataList) {
          Map<String, Object> recordData = applyDataMapping(dataMappingObj, (Map<String, Object>) item);
          String processedFilter = processFilter(filter, (Map<String, Object>) item);

          var updateBuilder = session.dsl().update(modelName).values(recordData);
          if (StringUtils.isNotBlank(processedFilter)) {
            updateBuilder.where(processedFilter);
          }
          updateBuilder.execute();
          affectedRows++;
        }
      } else if (inputData instanceof Map) {
        // 单条更新
        Map<String, Object> recordData = applyDataMapping(dataMappingObj, (Map<String, Object>) inputData);
        String processedFilter = processFilter(filter, (Map<String, Object>) inputData);

        var updateBuilder = session.dsl().update(modelName).values(recordData);
        if (StringUtils.isNotBlank(processedFilter)) {
          updateBuilder.where(processedFilter);
        }
        updateBuilder.execute();
        affectedRows = 1;
      } else {
        // 没有 inputPath，直接使用 dataMapping 和 filter
        Map<String, Object> recordData = applyDataMapping(dataMappingObj, contextData);
        String processedFilter = processFilter(filter, contextData);

        var updateBuilder = session.dsl().update(modelName).values(recordData);
        if (StringUtils.isNotBlank(processedFilter)) {
          updateBuilder.where(processedFilter);
        }
        updateBuilder.execute();
        affectedRows = 1;
      }
    }

    // 将结果存储到指定路径
    if (StringUtils.isNotBlank(resultPath)) {
      setResultPath(contextData, resultPath, affectedRows);
    }

    LOGGER.info("executeUpdateRecord: completed, affectedRows={}", affectedRows);
    return Map.of("affectedRows", affectedRows);
  }

  /**
   * 执行删除记录操作
   */
  private Object executeDeleteRecord(NodeInstanceBO nodeInstance, Map<String, Object> contextData) throws Exception {
    String datasourceName = getRequiredProperty(nodeInstance, "datasourceName");
    String modelName = getRequiredProperty(nodeInstance, "modelName");
    String filter = getOptionalProperty(nodeInstance, "filter");
    String resultPath = getOptionalProperty(nodeInstance, "resultPath");

    LOGGER.debug("executeDeleteRecord: datasource={}, model={}, filter={}, resultPath={}",
        datasourceName, modelName, filter, resultPath);

    int affectedRows = 0;

    try (Session session = sessionFactory.createSession(datasourceName)) {
      String processedFilter = processFilter(filter, contextData);

      var deleteBuilder = session.dsl().deleteFrom(modelName);
      if (StringUtils.isNotBlank(processedFilter)) {
        deleteBuilder.where(processedFilter);
      }
      deleteBuilder.execute();
      affectedRows = 1; // DSL 不返回实际删除行数，这里设为1表示执行成功
    }

    // 将结果存储到指定路径
    if (StringUtils.isNotBlank(resultPath)) {
      setResultPath(contextData, resultPath, affectedRows);
    }

    LOGGER.info("executeDeleteRecord: completed, affectedRows={}", affectedRows);
    return Map.of("affectedRows", affectedRows);
  }

  /**
   * 执行查询记录操作
   */
  private Object executeQueryRecord(NodeInstanceBO nodeInstance, Map<String, Object> contextData) throws Exception {
    String datasourceName = getRequiredProperty(nodeInstance, "datasourceName");
    String modelName = getRequiredProperty(nodeInstance, "modelName");
    String filter = getOptionalProperty(nodeInstance, "filter");
    String sortString = getOptionalProperty(nodeInstance, "sort");
    String resultPath = getOptionalProperty(nodeInstance, "resultPath");

    Integer page = null;
    Integer size = null;
    Object pageObj = nodeInstance.get("page");
    Object sizeObj = nodeInstance.get("size");

    if (pageObj != null && sizeObj != null) {
      page = Integer.parseInt(pageObj.toString());
      size = Integer.parseInt(sizeObj.toString());
    }

    LOGGER.debug("executeQueryRecord: datasource={}, model={}, filter={}, sort={}, page={}, size={}, resultPath={}",
        datasourceName, modelName, filter, sortString, page, size, resultPath);

    List<Map<String, Object>> records;

    try (Session session = sessionFactory.createSession(datasourceName)) {
      var queryBuilder = session.dsl().selectFrom(modelName);

      // 处理 filter
      if (StringUtils.isNotBlank(filter)) {
        String processedFilter = processFilter(filter, contextData);
        queryBuilder.where(processedFilter);
      }

      // 处理分页
      if (page != null) {
        queryBuilder.page(page, size);
      }

      // 处理排序
      if (StringUtils.isNotBlank(sortString)) {
        try {
          List<Query.OrderBy.Sort> sorts = JsonUtils.getInstance().parseToList(sortString, Query.OrderBy.Sort.class);
          Query.OrderBy orderBy = new Query.OrderBy();
          orderBy.getSorts().addAll(sorts);
          queryBuilder.orderBy(orderBy);
        } catch (Exception e) {
          LOGGER.error("Invalid sort string: {}", sortString, e);
        }
      }

      records = queryBuilder.enableNested().execute();
    }

    // 将结果存储到指定路径
    if (StringUtils.isNotBlank(resultPath)) {
      setResultPath(contextData, resultPath, records);
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

    if (dataMappingObj instanceof List) {
      List<?> mappingList = (List<?>) dataMappingObj;
      for (Object item : mappingList) {
        if (item instanceof Map) {
          Map<?, ?> mapping = (Map<?, ?>) item;
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
   * 处理 filter，支持变量替换
   */
  private String processFilter(String filter, Map<String, Object> contextData) {
    if (StringUtils.isBlank(filter)) {
      return filter;
    }

    // 使用 StringUtils.simpleRenderTemplate 替换 ${variable} 格式的变量
    // filter 中的 JSONPath 表达式由 DSL 查询引擎自己处理
    return StringUtils.simpleRenderTemplate(filter, contextData);
  }

  /**
   * 从 contextData 中根据 inputPath 提取数据
   */
  private Object getInputData(String inputPath, Map<String, Object> contextData) {
    if (StringUtils.isBlank(inputPath)) {
      return null;
    }

    try {
      // 使用 JsonPathUtils 提取数据
      String jsonData = JsonUtils.getInstance().stringify(contextData);
      return JsonPathUtils.evaluateJsonPath(inputPath, jsonData);
    } catch (Exception e) {
      LOGGER.error("Failed to extract data from inputPath: {}", inputPath, e);
      return null;
    }
  }

  /**
   * 将结果按照 resultPath 存入 contextData
   */
  private void setResultPath(Map<String, Object> contextData, String resultPath, Object value) {
    if (StringUtils.isBlank(resultPath) || !resultPath.startsWith("$.")) {
      return;
    }

    // 去掉开头的 "$."
    String path = resultPath.substring(2);
    String[] parts = path.split("\\.");

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

}
