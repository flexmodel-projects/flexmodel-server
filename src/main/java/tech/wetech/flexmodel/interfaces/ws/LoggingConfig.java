package tech.wetech.flexmodel.interfaces.ws;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author cjbi
 */
@ApplicationScoped
public class LoggingConfig {
  void onStart(@Observes StartupEvent ev) {
    Logger root = Logger.getLogger("");
    WebSocketLogHandler handler = new WebSocketLogHandler();
    handler.setFormatter(new SimpleFormatter());
    root.addHandler(handler);
  }
}
