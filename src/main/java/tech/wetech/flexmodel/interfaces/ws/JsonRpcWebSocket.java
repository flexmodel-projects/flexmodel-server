package tech.wetech.flexmodel.interfaces.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cjbi
 */
@ApplicationScoped
@ServerEndpoint("/api/f/json-rpc-ws")
public class JsonRpcWebSocket {

  private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

  /**
   * 新连接建立时触发
   */
  @OnOpen
  public void onOpen(Session session) {
    sessions.put(session.getId(), session);
    // 注册日志订阅
    WebSocketLogHandler.register(session);
    System.out.println("WebSocket connected: " + session.getId());
  }

  /**
   * 连接关闭时触发
   */
  @OnClose
  public void onClose(Session session) {
    sessions.remove(session.getId());
    // 取消日志订阅
    WebSocketLogHandler.unregister(session);
    System.out.println("WebSocket closed: " + session.getId());
  }

  /**
   * 接收客户端 JSON-RPC 消息
   */
  @OnMessage
  public void onMessage(String message, Session session) throws IOException {
    try {
      Map json = JsonUtils.getInstance().parseToObject(message, Map.class);


      String method = (String) json.get("method");
      Object params = json.get("params");
      String id = (String) json.get("id");

      Object result;
      switch (method) {
        case "ping" -> result = "pong";
        case "echo" -> result = params;
        default -> {
          sendError(session, id, "Unknown method: " + method);
          return;
        }
      }

      Map<String, Object> response = new HashMap<>();
      response.put("jsonrpc", "2.0");
      response.put("id", id);
      response.put("result", result);

      session.getBasicRemote().sendText(JsonUtils.getInstance().stringify(response));

    } catch (Exception e) {
      sendError(session, null, "Parse error: " + e.getMessage());
    }
  }

  private void sendError(Session session, String id, String message) throws IOException {
    Map<String, Object> error = new HashMap<>();
    error.put("jsonrpc", "2.0");
    error.put("id", id);
    error.put("error", Map.of("code", -32601, "message", message));
    session.getBasicRemote().sendText(JsonUtils.getInstance().stringify(error));
  }

}
