package tech.wetech.flexmodel.domain.model.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@ToString
@Getter
@Setter
public class ApiLog {

  private String id;
  private Level level = Level.INFO;
  private String uri;
  private Data data;
  private LocalDateTime createdAt;

  @Getter
  public enum Level {
    DEBUG,
    INFO,
    WARN,
    ERROR
  }

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  public static class Data {
    private String method;
    private String path;
    private String errors;
    private String referer;
    private String remoteIp;
    private int status;
    private String message;
    private String userAgent;
    private double execTime;
  }


}
