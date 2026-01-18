package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Config;
import dev.flexmodel.domain.model.settings.Settings;
import dev.flexmodel.domain.model.settings.SettingsRepository;
import dev.flexmodel.session.Session;
import dev.flexmodel.shared.utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class SettingsFmRepository implements SettingsRepository {

  @Inject
  Session session;

  @Override
  @SuppressWarnings("unchecked")
  public Settings saveSettings(Settings settings) {
    Map<String, Object> settingsMap = JsonUtils.getInstance().convertValue(settings, Map.class);
    settingsMap.forEach((key, value) -> {
      if (value != null) {

        Config config = session.dsl()
          .selectFrom(Config.class)
          .where(field(Config::getKey).eq(key))
          .executeOne();

        if (config == null) {
          config = new Config();
        }
        config.setKey(key);
        config.setValue(JsonUtils.getInstance().stringify(value));

        session.dsl()
          .mergeInto(Config.class)
          .values(config)
          .execute();
      }
    });
    return settings;
  }

  @Override
  public Settings getSettings() {

    List<Config> list = session.dsl()
      .selectFrom(Config.class)
      .execute();


    Map<String, Object> settingsMap = new HashMap<>();
    for (Config config : list) {
      settingsMap.put(config.getKey(), JsonUtils.getInstance().parseToObject(config.getValue(), Object.class));
    }
    return JsonUtils.getInstance().convertValue(settingsMap, Settings.class);
  }
}
