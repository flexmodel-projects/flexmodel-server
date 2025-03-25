package tech.wetech.flexmodel.domain.model.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

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
  private String url;
  private String errors;
  private String remoteIp;
  private int status;
  private String message;
  private double execTime;
  private Map<String, Object> request;
}
