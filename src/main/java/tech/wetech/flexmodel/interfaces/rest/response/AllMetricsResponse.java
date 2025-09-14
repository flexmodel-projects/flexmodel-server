package tech.wetech.flexmodel.interfaces.rest.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 所有监控指标响应DTO
 * 包含JVM、CPU、内存、线程、磁盘、网络等所有监控信息
 *
 * @author cjbi
 */
@Getter
@Setter
public class AllMetricsResponse {

  private JvmMetricsResponse jvm;

  private CpuMetricsResponse cpu;

  private MemoryMetricsResponse memory;

  private ThreadMetricsResponse threads;

  private DiskMetricsResponse disk;

  private NetworkMetricsResponse network;

  private SystemSummaryResponse summary;

  private PrometheusMetricsResponse prometheus;

  private long timestamp;

  private long processingTimeMs;

  private String error;

  /**
   * 创建成功响应
   */
  public static AllMetricsResponse success(JvmMetricsResponse jvm,
                                           CpuMetricsResponse cpu,
                                           MemoryMetricsResponse memory,
                                           ThreadMetricsResponse threads,
                                           DiskMetricsResponse disk,
                                           NetworkMetricsResponse network,
                                           SystemSummaryResponse summary,
                                           PrometheusMetricsResponse prometheus,
                                           long processingTimeMs) {
    AllMetricsResponse response = new AllMetricsResponse();
    response.setJvm(jvm);
    response.setCpu(cpu);
    response.setMemory(memory);
    response.setThreads(threads);
    response.setDisk(disk);
    response.setNetwork(network);
    response.setSummary(summary);
    response.setPrometheus(prometheus);
    response.setTimestamp(System.currentTimeMillis());
    response.setProcessingTimeMs(processingTimeMs);
    return response;
  }

  /**
   * 创建错误响应
   */
  public static AllMetricsResponse error(String errorMessage) {
    AllMetricsResponse response = new AllMetricsResponse();
    response.setError(errorMessage);
    response.setTimestamp(System.currentTimeMillis());
    return response;
  }
}
