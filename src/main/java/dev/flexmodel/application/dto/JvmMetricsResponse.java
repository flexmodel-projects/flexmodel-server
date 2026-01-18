package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * JVM监控响应DTO
 *
 * @author cjbi
 */
@Getter
@Setter
public class JvmMetricsResponse {

  private String name;

  private String version;

  private String vendor;

  private long uptime;

  private long startTime;

  private int loadedClassCount;

  private long totalLoadedClassCount;

  private long unloadedClassCount;

  private Map<String, GarbageCollectorInfo> garbageCollectors;

  private Map<String, String> systemProperties;


  @Getter
  @Setter
  public static class GarbageCollectorInfo {
    private long collectionCount;

    private long collectionTime;
  }
}
