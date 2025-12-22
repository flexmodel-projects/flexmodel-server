package tech.wetech.flexmodel.domain.model.idp.provider;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.domain.model.flow.shared.util.JavaScriptUtil;
import tech.wetech.flexmodel.domain.model.flow.shared.util.HttpScriptContext;

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
    try {
      HttpScriptContext scriptContext = new HttpScriptContext();
      scriptContext.setRequest(new HttpScriptContext.Request(param.getMethod(), param.getUrl(), param.getHeaders(), null, param.getQuery()));
      scriptContext.setResponse(new HttpScriptContext.Response(200, "success",null, null));
      Map<String, Object> contextMap = scriptContext.toMap();
      JavaScriptUtil.execute(script, contextMap);
      scriptContext.syncFromMap(contextMap);
      String message = (String) scriptContext.getResponse().body().get("message");
      boolean succcess = (boolean) scriptContext.getResponse().body().get("success");
      return new ValidateResult(succcess, message);
    } catch (Exception e) {
      return new ValidateResult(false, e.getMessage());
    } finally {
      JavaScriptUtil.cleanup();
    }
  }
}
