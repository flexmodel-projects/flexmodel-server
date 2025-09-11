package tech.wetech.flexmodel.domain.model.flow.shared.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import tech.wetech.flexmodel.domain.model.flow.dto.model.InstanceData;
import tech.wetech.flexmodel.domain.model.flow.shared.common.DataType;
import tech.wetech.flexmodel.shared.utils.CollectionUtils;
import tech.wetech.flexmodel.shared.utils.JsonUtils;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.util.List;
import java.util.Map;

public class InstanceDataUtil {

  private InstanceDataUtil() {
  }

  public static Map<String, InstanceData> getInstanceDataMap(List<InstanceData> instanceDataList) {
    if (CollectionUtils.isEmpty(instanceDataList)) {
      return Maps.newHashMap();
    }
    Map<String, InstanceData> instanceDataMap = Maps.newHashMap();
    instanceDataList.forEach(instanceData -> {
      instanceDataMap.put(instanceData.getKey(), instanceData);
    });
    return instanceDataMap;
  }

  public static Map<String, InstanceData> getInstanceDataMap(String instanceDataStr) {
    if (StringUtils.isBlank(instanceDataStr)) {
      return Maps.newHashMap();
    }
    List<InstanceData> instanceDataList = JsonUtils.getInstance().parseToList(instanceDataStr, InstanceData.class);
    return getInstanceDataMap(instanceDataList);
  }

  public static List<InstanceData> getInstanceDataList(Map<String, InstanceData> instanceDataMap) {
    if (instanceDataMap == null || instanceDataMap.isEmpty()) {
      return Lists.newArrayList();
    }
    List<InstanceData> instanceDataList = Lists.newArrayList();
    instanceDataMap.forEach((key, instanceData) -> {
      instanceDataList.add(instanceData);
    });
    return instanceDataList;
  }

  public static String getInstanceDataListStr(Map<String, InstanceData> instanceDataMap) {
    if (instanceDataMap == null || instanceDataMap.isEmpty()) {
      return JsonUtils.getInstance().stringify(Lists.newArrayList());
    }
    return JsonUtils.getInstance().stringify(instanceDataMap.values());
  }

  public static Map<String, Object> parseInstanceDataMap(Map<String, InstanceData> instanceDataMap) {
    if (instanceDataMap == null || instanceDataMap.isEmpty()) {
      return Maps.newHashMap();
    }
    Map<String, Object> dataMap = Maps.newHashMap();
    instanceDataMap.forEach((keyName, instanceData) -> {
      dataMap.put(keyName, parseInstanceData(instanceData));
    });
    return dataMap;
  }

  private static Object parseInstanceData(InstanceData instanceData) {
    if (instanceData == null) {
      return null;
    }
    String dataTypeStr = instanceData.getType();
    DataType dataType = DataType.getType(dataTypeStr);
    return instanceData.getValue();
  }
}
