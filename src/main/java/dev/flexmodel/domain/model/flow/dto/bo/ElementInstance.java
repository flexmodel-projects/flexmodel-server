package dev.flexmodel.domain.model.flow.dto.bo;

import java.util.List;
import java.util.Map;

public class ElementInstance {

  private String key;
  private String name;
  private Map<String, Object> properties;
  private int status;
  private String nodeInstanceId;
  private List<String> subFlowInstanceIdList;
  private List<ElementInstance> subElementInstanceList;
  private String instanceDataId;

  public ElementInstance() {
    super();
  }

  public ElementInstance(String key, int status) {
    this(key, status, null, null);
  }

  public ElementInstance(String key, int status, String nodeInstanceId, String instanceDataId) {
    super();
    this.key = key;
    this.status = status;
    this.nodeInstanceId = nodeInstanceId;
    this.instanceDataId = instanceDataId;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String Key) {
    this.key = Key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getNodeInstanceId() {
    return nodeInstanceId;
  }

  public void setNodeInstanceId(String nodeInstanceId) {
    this.nodeInstanceId = nodeInstanceId;
  }

  public List<String> getSubFlowInstanceIdList() {
    return subFlowInstanceIdList;
  }

  public void setSubFlowInstanceIdList(List<String> subFlowInstanceIdList) {
    this.subFlowInstanceIdList = subFlowInstanceIdList;
  }

  public String getInstanceDataId() {
    return instanceDataId;
  }

  public void setInstanceDataId(String instanceDataId) {
    this.instanceDataId = instanceDataId;
  }

  public List<ElementInstance> getSubElementInstanceList() {
    return subElementInstanceList;
  }

  public void setSubElementInstanceList(List<ElementInstance> subElementInstanceList) {
    this.subElementInstanceList = subElementInstanceList;
  }

  @Override
  public String toString() {
    return "ElementInstance{" +
           "Key='" + key + '\'' +
           ", name='" + name + '\'' +
           ", properties=" + properties +
           ", status=" + status +
           ", nodeInstanceId='" + nodeInstanceId + '\'' +
           ", subFlowInstanceIdList=" + subFlowInstanceIdList +
           ", subElementInstanceList=" + subElementInstanceList +
           ", instanceDataId='" + instanceDataId + '\'' +
           '}';
  }
}
