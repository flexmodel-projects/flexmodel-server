package tech.wetech.flexmodel.domain.model.flow.dto.result;

import tech.wetech.flexmodel.domain.model.flow.dto.bo.ElementInstance;
import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;

import java.util.List;

public class ElementInstanceListResult extends CommonResult {
  private List<ElementInstance> elementInstanceList;

  public ElementInstanceListResult(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  public List<ElementInstance> getElementInstanceList() {
    return elementInstanceList;
  }

  public void setElementInstanceList(List<ElementInstance> elementInstanceList) {
    this.elementInstanceList = elementInstanceList;
  }

  @Override
  public String toString() {
    return "ElementInstanceListResult{" +
           "elementInstanceList=" + elementInstanceList +
           '}';
  }
}
