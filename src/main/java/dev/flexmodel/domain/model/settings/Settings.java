package dev.flexmodel.domain.model.settings;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
@Getter
@Setter
public class Settings {

  private String appName = "Flexmodel";
  private Log log = new Log();
  private Security security = new Security();
  private Proxy proxy = new Proxy();

  @Getter
  @Setter
  public static class Log {
    /**
     * 最大保留天数
     */
    private int maxDays = 7;
    /**
     * 开启控制台日志
     */
    private boolean consoleLoggingEnabled = true;
  }

  @Getter
  @Setter
  public static class Security {
    private boolean rateLimitingEnabled = false;
    private int maxRequestCount = 500;
    private int intervalInSeconds = 60;
    private String graphqlEndpointPath = "/graphql";
    private String graphqlEndpointIdentityProvider;
    private String systemIdentityProvider = "default";
  }

  @Getter
  @Setter
  public static class Proxy {
    private boolean routesEnabled = false;
    private List<Route> routes = new ArrayList<>();
  }

  @Getter
  @Setter
  public static class Route {
    private String path;
    private String to;
  }

}
