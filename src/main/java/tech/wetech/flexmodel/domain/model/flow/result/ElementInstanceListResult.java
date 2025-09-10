package tech.wetech.flexmodel.domain.model.flow.result;

import com.google.common.base.MoreObjects;
import tech.wetech.flexmodel.domain.model.flow.bo.ElementInstance;
import tech.wetech.flexmodel.domain.model.flow.common.ErrorEnum;

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
    return MoreObjects.toStringHelper(this)
      .add("errCode", getErrCode())
      .add("errMsg", getErrMsg())
      .add("elementInstanceList", elementInstanceList)
      .toString();
  }
}
