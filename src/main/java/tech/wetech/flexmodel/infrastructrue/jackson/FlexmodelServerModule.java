package tech.wetech.flexmodel.infrastructrue.jackson;

import tech.wetech.flexmodel.domain.model.connect.database.Database;
import tech.wetech.flexmodel.domain.model.idp.provider.Provider;
import tech.wetech.flexmodel.infrastructrue.jackson.mixin.DatasourceDatabaseMixIn;
import tech.wetech.flexmodel.infrastructrue.jackson.mixin.IdentityProviderProviderMixIn;
import tech.wetech.flexmodel.supports.jackson.FlexmodelCoreModule;

/**
 * @author cjbi
 */
public class FlexmodelServerModule extends FlexmodelCoreModule {

  public FlexmodelServerModule() {
    super();
    this.setMixInAnnotation(Database.class, DatasourceDatabaseMixIn.class);
    this.setMixInAnnotation(Provider.class, IdentityProviderProviderMixIn.class);
  }
}
