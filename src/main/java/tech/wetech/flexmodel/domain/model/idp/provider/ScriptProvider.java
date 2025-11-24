package tech.wetech.flexmodel.domain.model.idp.provider;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.domain.model.flow.shared.util.JavaScriptUtil;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Getter
@Setter
@Slf4j
public class ScriptProvider implements Provider {

  private String script;

  @Override
  public String getType() {
    return "script";
  }

  @Override
  public ValidateResult validate(ValidateParam param) {
    String vScript = script + "\nauthenticate(request);";
    try {
      Map<String, Object> dataMap = new HashMap<>();
      dataMap.put("request", JsonUtils.getInstance().convertValue(param, Map.class));
      Object result = JavaScriptUtil.execute(vScript, dataMap);
      return JsonUtils.getInstance().convertValue(result, ValidateResult.class);
    } catch (Exception e) {
      return new ValidateResult(false, e.getMessage());
    }
  }
}
