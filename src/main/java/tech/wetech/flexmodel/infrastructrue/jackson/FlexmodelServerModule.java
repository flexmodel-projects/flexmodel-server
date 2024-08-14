package tech.wetech.flexmodel.infrastructrue.jackson;

import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.domain.model.idp.IdentityProvider;
import tech.wetech.flexmodel.infrastructrue.jackson.mixin.DatasourceDatabaseMixIn;
import tech.wetech.flexmodel.infrastructrue.jackson.mixin.IdentityProviderProviderMixIn;
import tech.wetech.flexmodel.supports.jackson.FlexmodelCoreModule;

/**
 * @author cjbi
 */
public class FlexmodelServerModule extends FlexmodelCoreModule {

  public FlexmodelServerModule() {
    super();
    this.setMixInAnnotation(Datasource.Database.class, DatasourceDatabaseMixIn.class);
    this.setMixInAnnotation(IdentityProvider.Provider.class, IdentityProviderProviderMixIn.class);
  }
}
