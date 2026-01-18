package dev.flexmodel.interfaces.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.interfaces.ws.dto.JsonRpcRequest;
import dev.flexmodel.interfaces.ws.dto.JsonRpcResponse;
import dev.flexmodel.shared.utils.JsonUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
@ServerEndpoint("/api/v1/json-rpc-ws")
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
    log.info("WebSocket connected: {}", session.getId());
  }

  /**
   * 连接关闭时触发
   */
  @OnClose
  public void onClose(Session session) {
    sessions.remove(session.getId());
    // 取消日志订阅
    WebSocketLogHandler.unregister(session);
    log.info("WebSocket closed: {}", session.getId());
  }

  /**
   * 接收客户端 JSON-RPC 消息
   */
  @OnMessage
  public void onMessage(String message, Session session) throws IOException {
    try {
      JsonRpcRequest request = JsonUtils.getInstance().parseToObject(message, JsonRpcRequest.class);
      String method = request.getMethod();
      Object params = request.getParams();
      String id = request.getId();

      Object result;
      switch (method) {
        case "ping" -> result = "pong";
        case "echo" -> result = params;
        default -> {
          sendError(session, id, "Unknown method: " + method);
          return;
        }
      }
      JsonRpcResponse response = JsonRpcResponse.success(id, method, result);
      session.getBasicRemote().sendText(response.toString());

    } catch (Exception e) {
      sendError(session, null, "Parse error: " + e.getMessage());
    }
  }

  private void sendError(Session session, String id, String message) throws IOException {
    JsonRpcResponse response = JsonRpcResponse.error(id, "-32601", message);
    session.getBasicRemote().sendText(response.toString());
  }

}
