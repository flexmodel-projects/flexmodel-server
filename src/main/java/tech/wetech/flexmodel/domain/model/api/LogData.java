package tech.wetech.flexmodel.domain.model.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author cjbi
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class LogData {
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
