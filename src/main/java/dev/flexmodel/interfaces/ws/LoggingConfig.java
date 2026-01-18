package dev.flexmodel.interfaces.ws;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import dev.flexmodel.shared.Constants;

import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author cjbi
 */
@ApplicationScoped
public class LoggingConfig {
  void onStart(@Observes StartupEvent ev) {
    Logger appLog = Logger.getLogger(Constants.APP_LOG_CATEGORY_NAME);
    WebSocketLogHandler handler = new WebSocketLogHandler();
    handler.setFormatter(new SimpleFormatter());
    appLog.addHandler(handler);
  }
}
