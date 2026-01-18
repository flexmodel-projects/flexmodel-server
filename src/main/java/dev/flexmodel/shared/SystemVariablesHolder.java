package dev.flexmodel.shared;

import org.eclipse.microprofile.config.ConfigProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class SystemVariablesHolder {

  @SuppressWarnings("all")
  public static Map<String, Object> getSystemVariables() {
    Map all = new HashMap<>();
    all.putAll(System.getenv());
    all.putAll(System.getProperties());
    Iterable<String> propertyNames = ConfigProvider.getConfig().getPropertyNames();
    for (String propertyName : propertyNames) {
      if (propertyName.isEmpty()) {
        continue;
      }
      ConfigProvider.getConfig()
        .getOptionalValue(propertyName, String.class)
        .ifPresent(val -> all.put(propertyName, val));
    }
    return all;
  }

}
