package dev.flexmodel.application.dto;

import lombok.*;

/**
 * 所有监控指标响应DTO
 * 包含JVM、CPU、内存、线程、磁盘、网络等所有监控信息
 *
 * @author cjbi
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllMetricsResponse {

  private JvmMetricsResponse jvm;

  private CpuMetricsResponse cpu;

  private MemoryMetricsResponse memory;

  private ThreadMetricsResponse threads;

  private DiskMetricsResponse disk;

  private NetworkMetricsResponse network;

  private SystemSummaryResponse summary;

  private PrometheusMetricsResponse prometheus;

  private FmMetricsResponse fm;

  private long timestamp;

  private long processingTimeMs;

  private String error;

}
