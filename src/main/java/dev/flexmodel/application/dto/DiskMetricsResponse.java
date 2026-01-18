package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 磁盘监控响应DTO
 *
 * @author cjbi
 */
@Getter
@Setter
public class DiskMetricsResponse {

  private Map<String, FileSystemInfo> fileSystems;

  private FileSystemInfo rootDirectory;

  private int totalFileSystems;

  private long totalSpace;

  private long totalUsableSpace;

  private long totalFreeSpace;

  private DiskIoInfo diskIo;

  @Getter
  @Setter
  public static class FileSystemInfo {
    private String name;

    private String type;

    private long totalSpace;

    private long usedSpace;

    private long usableSpace;

    private long freeSpace;

    private double usagePercentage;

    private boolean isReadOnly;

    private double totalSpaceGB;

    private double usedSpaceGB;

    private double usableSpaceGB;

    private double freeSpaceGB;

    private Long lastModified;

  }

  @Getter
  @Setter
  public static class DiskIoInfo {
    private long committedVirtualMemorySize;

    private ProcessIoInfo processIo;

    private FileSystemStatsInfo fileSystemStats;

    private JvmIoInfo jvmIo;

    private String error;

    private String fileSystemError;

  }

  @Getter
  @Setter
  public static class ProcessIoInfo {
    private long committedVirtualMemoryMB;

  }

  @Getter
  @Setter
  public static class FileSystemStatsInfo {
    private long totalSpace;

    private long freeSpace;

    private long usableSpace;

    private long lastModified;

    private double spaceUtilization;

  }

  @Getter
  @Setter
  public static class JvmIoInfo {
    private long maxMemory;

    private long totalMemory;

    private long freeMemory;

    private long usedMemory;

  }
}
