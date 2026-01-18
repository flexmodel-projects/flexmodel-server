package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 线程监控响应DTO
 *
 * @author cjbi
 */
@Getter
@Setter
public class ThreadMetricsResponse {

  private int threadCount;

  private int peakThreadCount;

  private int daemonThreadCount;

  private long totalStartedThreadCount;

  private long currentThreadCpuTime;

  private long currentThreadUserTime;

  private Map<String, Integer> threadStates;

  private Map<String, ThreadDetailInfo> threadDetails;

  @Getter
  @Setter
  public static class ThreadDetailInfo {
    private String name;

    private String state;

    private long blockedTime;

    private long blockedCount;

    private long waitedTime;

    private long waitedCount;

    private long cpuTime;

    private long userTime;

    private String lockName;

    private String lockOwnerName;

  }
}
