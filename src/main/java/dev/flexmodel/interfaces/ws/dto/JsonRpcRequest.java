package dev.flexmodel.interfaces.ws.dto;

import lombok.Getter;
import lombok.Setter;
import dev.flexmodel.shared.utils.JsonUtils;

/**
 * @author cjbi
 */
@Getter
@Setter
public class JsonRpcRequest {
  private String jsonrpc = "2.0";
  private String id;
  private String method;
  private Object params;

  @Override
  public String toString() {
    return JsonUtils.getInstance().stringify(this);
  }
}
