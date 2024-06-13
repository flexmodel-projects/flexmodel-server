package tech.wetech.flexmodel.infrastructrue.jackson;

import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.infrastructrue.jackson.mixin.DatasourceDatabaseMixIn;
import tech.wetech.flexmodel.supports.jackson.FlexModelCoreModule;

/**
 * @author cjbi
 */
public class FlexModelServerModule extends FlexModelCoreModule {

  public FlexModelServerModule() {
    super();
    this.setMixInAnnotation(Datasource.Database.class, DatasourceDatabaseMixIn.class);
  }
}
