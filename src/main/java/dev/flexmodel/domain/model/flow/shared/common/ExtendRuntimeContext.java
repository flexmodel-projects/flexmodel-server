package dev.flexmodel.domain.model.flow.shared.common;

import dev.flexmodel.domain.model.flow.dto.bo.NodeInstanceBO;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.exception.TurboException;

import java.io.Serializable;
import java.util.Map;

public class ExtendRuntimeContext implements Serializable {
  /**
   * 分支执行数据
   */
  private Map<String, Object> branchExecuteDataMap;
  /**
   * 分支挂起节点
   */
  private NodeInstanceBO branchSuspendNodeInstance;
  /**
   * 分支挂起节点
   */
  private FlowElement currentNodeModel;

  /**
   * 分支抛出的异常
   */
  private TurboException exception;

  public Map<String, Object> getBranchExecuteDataMap() {
    return branchExecuteDataMap;
  }

  public void setBranchExecuteDataMap(Map<String, Object> branchExecuteDataMap) {
    this.branchExecuteDataMap = branchExecuteDataMap;
  }

  public NodeInstanceBO getBranchSuspendNodeInstance() {
    return branchSuspendNodeInstance;
  }

  public void setBranchSuspendNodeInstance(NodeInstanceBO branchSuspendNodeInstance) {
    this.branchSuspendNodeInstance = branchSuspendNodeInstance;
  }

  public FlowElement getCurrentNodeModel() {
    return currentNodeModel;
  }

  public void setCurrentNodeModel(FlowElement currentNodeModel) {
    this.currentNodeModel = currentNodeModel;
  }

  public TurboException getException() {
    return exception;
  }

  public void setException(TurboException exception) {
    this.exception = exception;
  }
}
