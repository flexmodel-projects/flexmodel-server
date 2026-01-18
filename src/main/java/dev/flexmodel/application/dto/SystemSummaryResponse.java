package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 系统摘要响应DTO
 *
 * @author cjbi
 */
@Getter
@Setter
public class SystemSummaryResponse {

  private long systemTime;

  private long uptime;

  private int availableProcessors;

  private String osName;

  private String osVersion;

  private String osArch;

  private long heapUsedMB;

  private long heapMaxMB;

  private double heapUsagePercentage;

  private int threadCount;

  private int peakThreadCount;

  private int daemonThreadCount;

  private double diskTotalSpaceGB;

  private double diskUsableSpaceGB;

  private double diskFreeSpaceGB;

  private double diskUsagePercentage;

  private Integer totalNetworkInterfaces;

  private Integer upNetworkInterfaces;

  private String hostName;

  private String hostAddress;

  private String networkError;

  private String jvmName;

  private String jvmVersion;

  private String jvmVendor;

  private int loadedClassCount;

  private long totalLoadedClassCount;

  private long totalGcCount;

  private long totalGcTime;

  private Double systemCpuLoad;

  private Double processCpuLoad;

}
