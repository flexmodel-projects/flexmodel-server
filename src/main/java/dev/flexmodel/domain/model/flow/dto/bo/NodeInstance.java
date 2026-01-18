package dev.flexmodel.domain.model.flow.dto.bo;

import dev.flexmodel.domain.model.flow.dto.result.RuntimeResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeInstance extends ElementInstance {
  private String nodeInstanceId;
  private int flowElementType;
  private List<RuntimeResult> subNodeResultList;
  private LocalDateTime createTime;
  private LocalDateTime modifyTime;
  private Map<String, Object> properties = new HashMap<>();

  public String getNodeInstanceId() {
    return nodeInstanceId;
  }

  public void setNodeInstanceId(String nodeInstanceId) {
    this.nodeInstanceId = nodeInstanceId;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  public LocalDateTime getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(LocalDateTime modifyTime) {
    this.modifyTime = modifyTime;
  }

  public List<RuntimeResult> getSubNodeResultList() {
    return subNodeResultList;
  }

  public void setSubNodeResultList(List<RuntimeResult> subNodeResultList) {
    this.subNodeResultList = subNodeResultList;
  }

  public int getFlowElementType() {
    return flowElementType;
  }

  public void setFlowElementType(int flowElementType) {
    this.flowElementType = flowElementType;
  }

  @Override
  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public Object get(String key) {
    return properties.get(key);
  }

  public void put(String key, Object value) {
    properties.put(key, value);
  }

  @Override
  public String toString() {
    return "NodeInstance{" +
           "nodeInstanceId='" + nodeInstanceId + '\'' +
           ", flowElementType=" + flowElementType +
           ", subNodeResultList=" + subNodeResultList +
           ", createTime=" + createTime +
           ", modifyTime=" + modifyTime +
           ", properties=" + properties +
           '}';
  }
}
