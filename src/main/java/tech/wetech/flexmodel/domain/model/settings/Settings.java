package tech.wetech.flexmodel.domain.model.settings;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class Settings {

  private String appName = "Flexmodel";
  private Log log = new Log();
  private Security security = new Security();

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
    private boolean consoleLoggingEnabled = false;
  }

  @Getter
  @Setter
  public static class Security {
    private boolean apiRateLimitingEnabled = false;
    private int maxRequests = 500;
    private int limitRefreshPeriod = 60;
  }

}
