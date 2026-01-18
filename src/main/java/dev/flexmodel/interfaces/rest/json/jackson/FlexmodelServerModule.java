package dev.flexmodel.interfaces.rest.json.jackson;

import dev.flexmodel.domain.model.connect.database.Database;
import dev.flexmodel.domain.model.idp.provider.Provider;
import dev.flexmodel.domain.model.schedule.config.TriggerConfig;
import dev.flexmodel.interfaces.rest.json.jackson.mixin.DatasourceDatabaseMixIn;
import dev.flexmodel.interfaces.rest.json.jackson.mixin.IdentityProviderProviderMixIn;
import dev.flexmodel.interfaces.rest.json.jackson.mixin.ScheduledTriggerConfigMixIn;
import dev.flexmodel.supports.jackson.FlexmodelCoreModule;

/**
 * @author cjbi
 */
public class FlexmodelServerModule extends FlexmodelCoreModule {

  public FlexmodelServerModule() {
    super();
    this.setMixInAnnotation(Database.class, DatasourceDatabaseMixIn.class);
    this.setMixInAnnotation(Provider.class, IdentityProviderProviderMixIn.class);
    this.setMixInAnnotation(TriggerConfig.class, ScheduledTriggerConfigMixIn.class);
  }
}
