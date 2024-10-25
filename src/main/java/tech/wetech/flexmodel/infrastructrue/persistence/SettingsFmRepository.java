package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.dao.ConfigDAO;
import tech.wetech.flexmodel.codegen.entity.Config;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.domain.model.settings.SettingsRepository;
import tech.wetech.flexmodel.util.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@ApplicationScoped
public class SettingsFmRepository implements SettingsRepository {

  @Inject
  ConfigDAO configDAO;

  @Override
  @SuppressWarnings("unchecked")
  public Settings saveSettings(Settings settings) {
    Map<String, Object> settingsMap = JsonUtils.getInstance().convertValue(settings, Map.class);
    settingsMap.forEach((key, value) -> {
      if (value != null) {
        Config config = configDAO.find(query -> query.setFilter(f -> f.equalTo("key", key)))
          .stream().findFirst()
          .orElseGet(Config::new);
        config.setKey(key);
        config.setValue(JsonUtils.getInstance().stringify(value));
        configDAO.save(config);
      }
    });
    return settings;
  }

  @Override
  public Settings getSettings() {
    List<Config> list = configDAO.findAll();
    Map<String, Object> settingsMap = new HashMap<>();
    for (Config config : list) {
      settingsMap.put(config.getKey(), JsonUtils.getInstance().parseToObject(config.getValue(), Object.class));
    }
    return JsonUtils.getInstance().convertValue(settingsMap, Settings.class);
  }
}
