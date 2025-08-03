package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Config;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.domain.model.settings.SettingsRepository;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.util.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.query.expr.Expressions.field;

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
          .select()
          .from(Config.class)
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
      .select()
      .from(Config.class)
      .execute();


    Map<String, Object> settingsMap = new HashMap<>();
    for (Config config : list) {
      settingsMap.put(config.getKey(), JsonUtils.getInstance().parseToObject(config.getValue(), Object.class));
    }
    return JsonUtils.getInstance().convertValue(settingsMap, Settings.class);
  }
}
