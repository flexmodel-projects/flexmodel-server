package tech.wetech.flexmodel;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Optional;

/**
 * @author cjbi
 */
@ConfigMapping(prefix = "flexmodel")
public interface FlexmodelConfig {

  Datasource datasource();

  interface Datasource {

    @WithName("db-kind")
    String dbKind();

    String url();

    Optional<String> username();

    Optional<String> password();
  }
}
