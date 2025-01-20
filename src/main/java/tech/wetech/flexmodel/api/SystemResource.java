package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.wetech.flexmodel.application.SettingsApplicationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author cjbi
 */
@Slf4j
@Path("/api/system")
public class SystemResource {

  @Inject
  SettingsApplicationService settingsApplicationService;

  @GET
  @Path("/config")
  public Map<String, Object> getConfig() {
    Map<String, Object> all = new HashMap<>();
    Map<String, String> env = System.getenv();
    Properties properties = System.getProperties();
    Map<String, Object> appConfig = new HashMap<>();
    Iterable<String> propertyNames = ConfigProvider.getConfig().getPropertyNames();
    for (String propertyName : propertyNames) {
      try {

        boolean hasKey = false;
        Set<String> envKeys = env.keySet();
        Set<Object> propKeys = properties.keySet();
        for (String key : envKeys) {
          if (propertyName.equalsIgnoreCase(key)) {
            hasKey = true;
            break;
          }
        }
        if (!hasKey) {
          for (Object key : propKeys) {
            if (propertyName.equalsIgnoreCase(key.toString())) {
              hasKey = true;
              break;
            }
          }
        }
        if (!hasKey) {
          appConfig.put(propertyName, ConfigProvider.getConfig().getValue(propertyName, String.class));
        }
      } catch (Exception e) {
        log.error("get config error, key={}, message={}", propertyName, e.getMessage(), e);
      }
    }
    all.put("env", env);
    all.put("properties", properties);
    all.put("application", appConfig);
    all.put("settings", settingsApplicationService.getSettings());
    return all;
  }

}
