package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 内存监控响应DTO
 *
 * @author cjbi
 */
@Getter
@Setter
public class MemoryMetricsResponse {

  private MemoryInfo heap;

  private MemoryInfo nonHeap;

  private Map<String, MemoryPoolInfo> memoryPools;

  @Getter
  @Setter
  public static class MemoryInfo {
    private long init;

    private long used;

    private long committed;

    private long max;

    private double usagePercentage;

  }

  @Getter
  @Setter
  public static class MemoryPoolInfo {
    private long init;

    private long used;

    private long committed;

    private long max;

    private double usagePercentage;

    private String type;

    private String memoryManagerNames;

  }
}
