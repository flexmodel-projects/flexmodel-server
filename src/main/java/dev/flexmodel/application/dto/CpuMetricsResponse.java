package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * CPU监控响应DTO
 *
 * @author cjbi
 */
@Getter
@Setter
public class CpuMetricsResponse {

  private int availableProcessors;

  private String architecture;

  private String name;

  private String version;

  private Double systemCpuLoad;

  private Double processCpuLoad;

  private Double systemLoadAverage;

  private Long totalPhysicalMemorySize;

  private Long freePhysicalMemorySize;

  private Long totalSwapSpaceSize;

  private Long freeSwapSpaceSize;

  private Long committedVirtualMemorySize;

  private long maxMemory;

  private long totalMemory;

  private long freeMemory;

  private long usedMemory;

}
