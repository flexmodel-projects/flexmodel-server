package tech.wetech.flexmodel;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import io.smallrye.config.WithUnnamedKey;

import java.util.Map;
import java.util.Optional;

/**
 * @author cjbi
 */
@ConfigMapping(prefix = "flexmodel")
public interface FlexmodelConfig {

  @WithName("datasource")
  @WithUnnamedKey("system")
  Map<String, DatasourceConfig> datasources();

  @WithDefault("/api/v1")
  @WithName("context-path")
  String contextPath();

  interface DatasourceConfig {

    @WithName("db-kind")
    String dbKind();

    String url();

    Optional<String> username();

    Optional<String> password();
  }
}
