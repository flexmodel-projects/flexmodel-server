package tech.wetech.flexmodel.interfaces.ws;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logmanager.ExtLogRecord;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

@Slf4j
public class WebSocketLogHandler extends Handler {

  private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

  public static void register(Session session) {
    sessions.add(session);
  }

  public static void unregister(Session session) {
    sessions.remove(session);
  }

  @Override
  public void publish(LogRecord record) {
    if (sessions.isEmpty()) return;

    try {
      String message = record.getMessage();
      Map<String, Object> map = new HashMap<>();
      map.put("jsonrpc", "2.0");
      map.put("method", "logEvent");
      map.put("thread", ((ExtLogRecord) record).getThreadName());
      map.put("level", record.getLevel().toString());
      map.put("logger", record.getLoggerName().replace("tech.wetech.flexmodel.", ""));
      map.put("message", message);


      String payload = JsonUtils.getInstance().stringify(map);

      for (Session s : sessions) {
        if (s.isOpen()) {
          s.getBasicRemote().sendText(payload);
        }
      }
    } catch (IOException e) {
      log.error("Error sending log event to WebSocket", e);
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() throws SecurityException {
  }
}
