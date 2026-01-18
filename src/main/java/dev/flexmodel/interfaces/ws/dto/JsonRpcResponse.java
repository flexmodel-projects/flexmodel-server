package dev.flexmodel.interfaces.ws.dto;

import lombok.Getter;
import lombok.Setter;
import dev.flexmodel.shared.utils.JsonUtils;

/**
 * @author cjbi
 */
@Getter
@Setter
public class JsonRpcResponse {
  private String jsonrpc = "2.0";
  private String id;
  private Error error;
  private Result result;

  public record Result(String messageType, Object object) {
  }

  public record Error(String code, String message) {
  }

  public static JsonRpcResponse success(String id, String messageType, Object data) {
    JsonRpcResponse response = new JsonRpcResponse();
    response.setId(id);
    response.setResult(new Result(messageType, data));
    return response;
  }

  public static JsonRpcResponse error(String id, String code, String message) {
    JsonRpcResponse response = new JsonRpcResponse();
    response.setId(id);
    response.setError(new Error(code, message));
    return response;
  }

  @Override
  public String toString() {
    return JsonUtils.getInstance().stringify(this);
  }

}
