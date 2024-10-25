package tech.wetech.flexmodel.domain.model.settings;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
@Getter
@Setter
public class Settings {

  private Log log = new Log();

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
    private boolean enableConsoleLogging = false;
  }

}
