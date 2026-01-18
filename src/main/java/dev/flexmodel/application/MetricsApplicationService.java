package dev.flexmodel.application;

import dev.flexmodel.application.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.application.dto.*;
import dev.flexmodel.codegen.entity.FlowDefinition;
import dev.flexmodel.codegen.entity.JobExecutionLog;
import dev.flexmodel.domain.model.api.ApiDefinitionService;
import dev.flexmodel.domain.model.api.ApiRequestLogService;
import dev.flexmodel.domain.model.connect.DatasourceService;
import dev.flexmodel.domain.model.flow.service.FlowDefinitionService;
import dev.flexmodel.domain.model.flow.service.FlowInstanceService;
import dev.flexmodel.domain.model.modeling.ModelService;
import dev.flexmodel.domain.model.schedule.JobExecutionLogService;
import dev.flexmodel.domain.model.schedule.TriggerService;
import dev.flexmodel.quarkus.session.SessionManaged;
import dev.flexmodel.query.Expressions;
import dev.flexmodel.shared.SessionContextHolder;

import java.io.File;
import java.lang.management.*;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.flexmodel.query.Expressions.TRUE;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
@SessionManaged
public class MetricsApplicationService {

  private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
  @Inject
  ApiDefinitionService apiDefinitionService;
  @Inject
  ApiRequestLogService apiLogService;
  @Inject
  DatasourceService datasourceService;
  @Inject
  ModelService modelService;
  @Inject
  FlowInstanceService flowInstanceService;
  @Inject
  FlowDefinitionService flowDefService;
  @Inject
  TriggerService triggerService;
  @Inject
  JobExecutionLogService jobExecutionLogService;

  public FmMetricsResponse getFmMetrics(String projectId) {
    try {
      Integer modelCount = modelService.count(projectId);
      Integer customApiCount = apiDefinitionService.count(projectId);
      Integer datasourceCount = datasourceService.count(projectId);
      long reqLogCount = apiLogService.count(projectId, TRUE);
      long flowDefCount = flowDefService.count(projectId, Expressions.field(FlowDefinition::getIsDeleted).eq(false));
      long flowInsCount = flowInstanceService.count(projectId, TRUE);
      long triggerCount = triggerService.count(projectId, TRUE);
      long jobSuccessCount = jobExecutionLogService.count(Expressions.field(JobExecutionLog::getExecutionStatus).eq("SUCCESS"));
      long jobFailureCount = jobExecutionLogService.count(Expressions.field(JobExecutionLog::getExecutionStatus).eq("FAILED"));

      return FmMetricsResponse.builder()
        .dataSourceCount(datasourceCount)
        .customApiCount(customApiCount)
        .requestCount((int) reqLogCount)
        .flowDefCount((int) flowDefCount)
        .flowExecCount((int) flowInsCount)
        .modelCount(modelCount)
        .triggerTotalCount((int) triggerCount)
        .jobSuccessCount((int) jobSuccessCount)
        .jobFailureCount((int) jobFailureCount)
        .build();

    } catch (Exception e) {
      log.error("get api fm metrics error", e);
      throw new RuntimeException(e);
    }
  }

  public JvmMetricsResponse getJvmMetrics() {
    try {
      JvmMetricsResponse jvmInfo = new JvmMetricsResponse();

      // JVM基本信息
      RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
      jvmInfo.setName(runtimeBean.getVmName());
      jvmInfo.setVersion(runtimeBean.getVmVersion());
      jvmInfo.setVendor(runtimeBean.getVmVendor());
      jvmInfo.setUptime(runtimeBean.getUptime());
      jvmInfo.setStartTime(runtimeBean.getStartTime());

      // 类加载信息
      ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
      jvmInfo.setLoadedClassCount(classBean.getLoadedClassCount());
      jvmInfo.setTotalLoadedClassCount(classBean.getTotalLoadedClassCount());
      jvmInfo.setUnloadedClassCount(classBean.getUnloadedClassCount());

      // 垃圾回收信息
      List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
      Map<String, JvmMetricsResponse.GarbageCollectorInfo> gcInfo = new HashMap<>();
      for (GarbageCollectorMXBean gcBean : gcBeans) {
        JvmMetricsResponse.GarbageCollectorInfo gcDetails = new JvmMetricsResponse.GarbageCollectorInfo();
        gcDetails.setCollectionCount(gcBean.getCollectionCount());
        gcDetails.setCollectionTime(gcBean.getCollectionTime());
        gcInfo.put(gcBean.getName(), gcDetails);
      }
      jvmInfo.setGarbageCollectors(gcInfo);

      // 系统属性
      Map<String, String> systemProperties = new HashMap<>();
      systemProperties.put("java.version", System.getProperty("java.version"));
      systemProperties.put("java.vendor", System.getProperty("java.vendor"));
      systemProperties.put("os.name", System.getProperty("os.name"));
      systemProperties.put("os.version", System.getProperty("os.version"));
      systemProperties.put("os.arch", System.getProperty("os.arch"));
      jvmInfo.setSystemProperties(systemProperties);

      return jvmInfo;
    } catch (Exception e) {
      throw new RuntimeException("获取JVM信息失败: " + e.getMessage());
    }
  }

  public CpuMetricsResponse getCpuMetrics() {
    try {
      CpuMetricsResponse cpuInfo = new CpuMetricsResponse();

      OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

      // CPU基本信息
      cpuInfo.setAvailableProcessors(osBean.getAvailableProcessors());
      cpuInfo.setArchitecture(osBean.getArch());
      cpuInfo.setName(osBean.getName());
      cpuInfo.setVersion(osBean.getVersion());

      // CPU使用率（需要额外计算）
      if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
        com.sun.management.OperatingSystemMXBean sunOsBean =
          (com.sun.management.OperatingSystemMXBean) osBean;

        try {
          cpuInfo.setSystemCpuLoad(
            Double.parseDouble(decimalFormat.format(sunOsBean.getSystemCpuLoad() * 100)));
          cpuInfo.setProcessCpuLoad(
            Double.parseDouble(decimalFormat.format(sunOsBean.getProcessCpuLoad() * 100)));
        } catch (Exception e) {
          // 忽略CPU使用率获取失败
          cpuInfo.setSystemCpuLoad(0.0);
          cpuInfo.setProcessCpuLoad(0.0);
        }

        cpuInfo.setSystemLoadAverage(sunOsBean.getSystemLoadAverage());

        try {
          cpuInfo.setTotalPhysicalMemorySize(sunOsBean.getTotalPhysicalMemorySize());
          cpuInfo.setFreePhysicalMemorySize(sunOsBean.getFreePhysicalMemorySize());
          cpuInfo.setTotalSwapSpaceSize(sunOsBean.getTotalSwapSpaceSize());
          cpuInfo.setFreeSwapSpaceSize(sunOsBean.getFreeSwapSpaceSize());
          cpuInfo.setCommittedVirtualMemorySize(sunOsBean.getCommittedVirtualMemorySize());
        } catch (Exception e) {
          // 忽略内存信息获取失败
          cpuInfo.setTotalPhysicalMemorySize(0L);
          cpuInfo.setFreePhysicalMemorySize(0L);
          cpuInfo.setTotalSwapSpaceSize(0L);
          cpuInfo.setFreeSwapSpaceSize(0L);
          cpuInfo.setCommittedVirtualMemorySize(0L);
        }
      }

      // 运行时信息
      Runtime runtime = Runtime.getRuntime();
      cpuInfo.setMaxMemory(runtime.maxMemory());
      cpuInfo.setTotalMemory(runtime.totalMemory());
      cpuInfo.setFreeMemory(runtime.freeMemory());
      cpuInfo.setUsedMemory(runtime.totalMemory() - runtime.freeMemory());

      return cpuInfo;
    } catch (Exception e) {
      throw new RuntimeException("获取CPU信息失败: " + e.getMessage());
    }
  }

  public MemoryMetricsResponse getMemoryMetrics() {
    try {
      MemoryMetricsResponse memoryInfo = new MemoryMetricsResponse();

      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

      // 堆内存信息
      MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
      MemoryMetricsResponse.MemoryInfo heapInfo = new MemoryMetricsResponse.MemoryInfo();
      heapInfo.setInit(heapUsage.getInit());
      heapInfo.setUsed(heapUsage.getUsed());
      heapInfo.setCommitted(heapUsage.getCommitted());
      heapInfo.setMax(heapUsage.getMax());
      heapInfo.setUsagePercentage(
        Double.parseDouble(decimalFormat.format((double) heapUsage.getUsed() / heapUsage.getCommitted() * 100)));
      memoryInfo.setHeap(heapInfo);

      // 非堆内存信息
      MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
      MemoryMetricsResponse.MemoryInfo nonHeapInfo = new MemoryMetricsResponse.MemoryInfo();
      nonHeapInfo.setInit(nonHeapUsage.getInit());
      nonHeapInfo.setUsed(nonHeapUsage.getUsed());
      nonHeapInfo.setCommitted(nonHeapUsage.getCommitted());
      nonHeapInfo.setMax(nonHeapUsage.getMax());
      if (nonHeapUsage.getCommitted() > 0) {
        nonHeapInfo.setUsagePercentage(
          Double.parseDouble(decimalFormat.format((double) nonHeapUsage.getUsed() / nonHeapUsage.getCommitted() * 100)));
      } else {
        nonHeapInfo.setUsagePercentage(0);
      }
      memoryInfo.setNonHeap(nonHeapInfo);

      // 内存池详细信息
      List<java.lang.management.MemoryPoolMXBean> memoryPools =
        ManagementFactory.getMemoryPoolMXBeans();
      Map<String, MemoryMetricsResponse.MemoryPoolInfo> memoryPoolsInfo = new HashMap<>();
      for (java.lang.management.MemoryPoolMXBean pool : memoryPools) {
        MemoryMetricsResponse.MemoryPoolInfo poolInfo = new MemoryMetricsResponse.MemoryPoolInfo();
        MemoryUsage poolUsage = pool.getUsage();
        if (poolUsage != null) {
          poolInfo.setInit(poolUsage.getInit());
          poolInfo.setUsed(poolUsage.getUsed());
          poolInfo.setCommitted(poolUsage.getCommitted());
          poolInfo.setMax(poolUsage.getMax());
          if (poolUsage.getCommitted() > 0) {
            poolInfo.setUsagePercentage(
              Double.parseDouble(decimalFormat.format((double) poolUsage.getUsed() / poolUsage.getCommitted() * 100)));
          } else {
            poolInfo.setUsagePercentage(0);
          }
        }
        poolInfo.setType(pool.getType().toString());
        poolInfo.setMemoryManagerNames(String.join(", ", pool.getMemoryManagerNames()));
        memoryPoolsInfo.put(pool.getName(), poolInfo);
      }
      memoryInfo.setMemoryPools(memoryPoolsInfo);

      return memoryInfo;
    } catch (Exception e) {
      throw new RuntimeException("获取内存信息失败: " + e.getMessage());
    }
  }

  public ThreadMetricsResponse getThreadMetrics() {
    try {
      ThreadMetricsResponse threadInfo = new ThreadMetricsResponse();

      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

      // 线程基本统计
      threadInfo.setThreadCount(threadBean.getThreadCount());
      threadInfo.setPeakThreadCount(threadBean.getPeakThreadCount());
      threadInfo.setDaemonThreadCount(threadBean.getDaemonThreadCount());
      threadInfo.setTotalStartedThreadCount(threadBean.getTotalStartedThreadCount());
      threadInfo.setCurrentThreadCpuTime(threadBean.getCurrentThreadCpuTime());
      threadInfo.setCurrentThreadUserTime(threadBean.getCurrentThreadUserTime());

      // 线程状态统计
      Map<Thread.State, Integer> threadStates = new HashMap<>();
      for (Thread.State state : Thread.State.values()) {
        threadStates.put(state, 0);
      }

      // 获取所有线程信息
      long[] threadIds = threadBean.getAllThreadIds();
      Map<String, ThreadMetricsResponse.ThreadDetailInfo> threadsDetails = new HashMap<>();

      for (long threadId : threadIds) {
        java.lang.management.ThreadInfo info = threadBean.getThreadInfo(threadId);
        if (info != null) {
          Thread.State state = info.getThreadState();
          threadStates.put(state, threadStates.get(state) + 1);

          // 线程详细信息
          ThreadMetricsResponse.ThreadDetailInfo threadDetail = new ThreadMetricsResponse.ThreadDetailInfo();
          threadDetail.setName(info.getThreadName());
          threadDetail.setState(state.toString());
          threadDetail.setBlockedTime(info.getBlockedTime());
          threadDetail.setBlockedCount(info.getBlockedCount());
          threadDetail.setWaitedTime(info.getWaitedTime());
          threadDetail.setWaitedCount(info.getWaitedCount());
          threadDetail.setCpuTime(threadBean.getThreadCpuTime(threadId));
          threadDetail.setUserTime(threadBean.getThreadUserTime(threadId));

          if (info.getLockName() != null) {
            threadDetail.setLockName(info.getLockName());
          }
          if (info.getLockOwnerName() != null) {
            threadDetail.setLockOwnerName(info.getLockOwnerName());
          }

          threadsDetails.put(String.valueOf(threadId), threadDetail);
        }
      }

      // 线程状态统计
      Map<String, Integer> stateStats = new HashMap<>();
      for (Map.Entry<Thread.State, Integer> entry : threadStates.entrySet()) {
        stateStats.put(entry.getKey().toString(), entry.getValue());
      }
      threadInfo.setThreadStates(stateStats);
      threadInfo.setThreadDetails(threadsDetails);

      return threadInfo;
    } catch (Exception e) {
      throw new RuntimeException("获取线程信息失败: " + e.getMessage());
    }
  }

  public DiskMetricsResponse getDiskMetrics() {
    try {
      DiskMetricsResponse diskInfo = new DiskMetricsResponse();

      // 获取所有文件系统
      List<FileStore> fileStores = new ArrayList<>();
      for (FileStore fileStore : FileSystems.getDefault().getFileStores()) {
        fileStores.add(fileStore);
      }
      Map<String, DiskMetricsResponse.FileSystemInfo> fileSystemsInfo = new HashMap<>();

      for (FileStore fileStore : fileStores) {
        try {
          DiskMetricsResponse.FileSystemInfo fsInfo = new DiskMetricsResponse.FileSystemInfo();

          // 基本信息
          fsInfo.setName(fileStore.name());
          fsInfo.setType(fileStore.type());

          // 容量信息
          long totalSpace = fileStore.getTotalSpace();
          long usableSpace = fileStore.getUsableSpace();
          long usedSpace = totalSpace - usableSpace;

          fsInfo.setTotalSpace(totalSpace);
          fsInfo.setUsedSpace(usedSpace);
          fsInfo.setUsableSpace(usableSpace);
          fsInfo.setFreeSpace(fileStore.getUnallocatedSpace());

          // 计算使用率
          if (totalSpace > 0) {
            double usagePercentage = (double) usedSpace / totalSpace * 100;
            fsInfo.setUsagePercentage(
              Double.parseDouble(decimalFormat.format(usagePercentage)));
          } else {
            fsInfo.setUsagePercentage(0.0);
          }

          // 文件系统属性
          fsInfo.setReadOnly(fileStore.isReadOnly());

          // 存储单位转换
          fsInfo.setTotalSpaceGB(totalSpace / (1024.0 * 1024.0 * 1024.0));
          fsInfo.setUsedSpaceGB(usedSpace / (1024.0 * 1024.0 * 1024.0));
          fsInfo.setUsableSpaceGB(usableSpace / (1024.0 * 1024.0 * 1024.0));

          fileSystemsInfo.put(fileStore.name(), fsInfo);
        } catch (Exception e) {
          // 跳过无法访问的文件系统
          continue;
        }
      }

      diskInfo.setFileSystems(fileSystemsInfo);

      // 根目录信息
      File root = File.listRoots()[0];
      DiskMetricsResponse.FileSystemInfo rootInfo = new DiskMetricsResponse.FileSystemInfo();
      rootInfo.setTotalSpace(root.getTotalSpace());
      rootInfo.setFreeSpace(root.getFreeSpace());
      rootInfo.setUsableSpace(root.getUsableSpace());
      rootInfo.setTotalSpaceGB(root.getTotalSpace() / (1024.0 * 1024.0 * 1024.0));
      rootInfo.setFreeSpaceGB(root.getFreeSpace() / (1024.0 * 1024.0 * 1024.0));
      rootInfo.setUsableSpaceGB(root.getUsableSpace() / (1024.0 * 1024.0 * 1024.0));

      if (root.getTotalSpace() > 0) {
        double usagePercentage = (double) (root.getTotalSpace() - root.getUsableSpace()) / root.getTotalSpace() * 100;
        rootInfo.setUsagePercentage(
          Double.parseDouble(decimalFormat.format(usagePercentage)));
      } else {
        rootInfo.setUsagePercentage(0.0);
      }

      diskInfo.setRootDirectory(rootInfo);

      // 统计信息
      diskInfo.setTotalFileSystems(fileStores.size());
      diskInfo.setTotalSpace(root.getTotalSpace());
      diskInfo.setTotalUsableSpace(root.getUsableSpace());
      diskInfo.setTotalFreeSpace(root.getFreeSpace());

      // 添加磁盘I/O信息
      DiskMetricsResponse.DiskIoInfo diskIoInfo = new DiskMetricsResponse.DiskIoInfo();

      // 使用OperatingSystemMXBean获取磁盘I/O信息
      if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean) {
        com.sun.management.OperatingSystemMXBean sunOsBean =
          (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        try {
          // 磁盘I/O统计
          diskIoInfo.setCommittedVirtualMemorySize(sunOsBean.getCommittedVirtualMemorySize());

          // 尝试获取进程级别的I/O信息（如果可用）
          DiskMetricsResponse.ProcessIoInfo processIo = new DiskMetricsResponse.ProcessIoInfo();
          processIo.setCommittedVirtualMemoryMB(sunOsBean.getCommittedVirtualMemorySize() / 1024 / 1024);

          diskIoInfo.setProcessIo(processIo);
        } catch (Exception e) {
          // 如果无法获取详细信息，提供基本信息
          diskIoInfo.setError("无法获取详细I/O信息: " + e.getMessage());
        }
      }

      // 添加文件系统级别的统计
      DiskMetricsResponse.FileSystemStatsInfo fileSystemStats = new DiskMetricsResponse.FileSystemStatsInfo();
      try {
        fileSystemStats.setTotalSpace(root.getTotalSpace());
        fileSystemStats.setFreeSpace(root.getFreeSpace());
        fileSystemStats.setUsableSpace(root.getUsableSpace());
        fileSystemStats.setLastModified(root.lastModified());

        // 计算空间变化趋势（需要历史数据，这里提供当前状态）
        fileSystemStats.setSpaceUtilization(
          (root.getTotalSpace() - root.getUsableSpace()) * 100.0 / root.getTotalSpace());

        diskIoInfo.setFileSystemStats(fileSystemStats);
      } catch (Exception e) {
        diskIoInfo.setFileSystemError("无法获取文件系统统计: " + e.getMessage());
      }

      // 添加JVM级别的I/O信息
      DiskMetricsResponse.JvmIoInfo jvmIo = new DiskMetricsResponse.JvmIoInfo();
      Runtime runtime = Runtime.getRuntime();
      jvmIo.setMaxMemory(runtime.maxMemory());
      jvmIo.setTotalMemory(runtime.totalMemory());
      jvmIo.setFreeMemory(runtime.freeMemory());
      jvmIo.setUsedMemory(runtime.totalMemory() - runtime.freeMemory());

      diskIoInfo.setJvmIo(jvmIo);

      // 将磁盘I/O信息添加到磁盘信息中
      diskInfo.setDiskIo(diskIoInfo);

      return diskInfo;
    } catch (Exception e) {
      throw new RuntimeException("获取磁盘信息失败: " + e.getMessage());
    }
  }

  public NetworkMetricsResponse getNetworkMetrics() {
    try {
      NetworkMetricsResponse networkInfo = new NetworkMetricsResponse();

      // 并行处理网络接口信息和本机信息
      CompletableFuture<Map<String, NetworkMetricsResponse.NetworkInterfaceInfo>> interfacesFuture =
        CompletableFuture.supplyAsync(this::getNetworkInterfacesInfo);

      CompletableFuture<NetworkMetricsResponse.LocalhostInfo> localhostFuture =
        CompletableFuture.supplyAsync(this::getLocalhostInfo);

      // 等待所有任务完成
      CompletableFuture.allOf(interfacesFuture, localhostFuture).join();

      // 设置结果
      Map<String, NetworkMetricsResponse.NetworkInterfaceInfo> interfacesInfo = interfacesFuture.get();
      networkInfo.setInterfaces(interfacesInfo);
      networkInfo.setTotalInterfaces(interfacesInfo.size());

      try {
        NetworkMetricsResponse.LocalhostInfo localInfo = localhostFuture.get();
        networkInfo.setLocalhost(localInfo);
      } catch (Exception e) {
        networkInfo.setLocalhostError("无法获取本机信息: " + e.getMessage());
      }

      // 网络统计信息
      NetworkMetricsResponse.NetworkStatsInfo networkStats = new NetworkMetricsResponse.NetworkStatsInfo();
      networkStats.setActiveInterfaces(interfacesInfo.size());
      networkStats.setTimestamp(System.currentTimeMillis());
      networkInfo.setStats(networkStats);

      return networkInfo;
    } catch (Exception e) {
      throw new RuntimeException("获取网络信息失败: " + e.getMessage());
    }
  }

  private Map<String, NetworkMetricsResponse.NetworkInterfaceInfo> getNetworkInterfacesInfo() {
    Map<String, NetworkMetricsResponse.NetworkInterfaceInfo> interfacesInfo = new HashMap<>();

    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      List<NetworkInterface> interfaceList = new ArrayList<>();

      // 先收集所有接口，避免在遍历时修改
      while (networkInterfaces.hasMoreElements()) {
        interfaceList.add(networkInterfaces.nextElement());
      }

      // 并行处理网络接口
      List<CompletableFuture<Void>> futures = interfaceList.stream()
        .map(networkInterface -> CompletableFuture.runAsync(() -> {
          try {
            NetworkMetricsResponse.NetworkInterfaceInfo interfaceInfo = processNetworkInterface(networkInterface);
            synchronized (interfacesInfo) {
              interfacesInfo.put(networkInterface.getName(), interfaceInfo);
            }
          } catch (Exception e) {
            // 跳过无法访问的接口，记录日志但不影响其他接口
            System.err.println("处理网络接口 " + networkInterface.getName() + " 时出错: " + e.getMessage());
          }
        }))
        .collect(Collectors.toList());

      // 等待所有接口处理完成
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    } catch (Exception e) {
      System.err.println("获取网络接口列表失败: " + e.getMessage());
    }

    return interfacesInfo;
  }

  private NetworkMetricsResponse.NetworkInterfaceInfo processNetworkInterface(NetworkInterface networkInterface) {
    NetworkMetricsResponse.NetworkInterfaceInfo interfaceInfo = new NetworkMetricsResponse.NetworkInterfaceInfo();

    try {
      // 基本信息（快速获取）
      interfaceInfo.setName(networkInterface.getName());
      interfaceInfo.setDisplayName(networkInterface.getDisplayName());
      interfaceInfo.setUp(networkInterface.isUp());
      interfaceInfo.setLoopback(networkInterface.isLoopback());
      interfaceInfo.setVirtual(networkInterface.isVirtual());
      interfaceInfo.setPointToPoint(networkInterface.isPointToPoint());
      interfaceInfo.setSupportsMulticast(networkInterface.supportsMulticast());

      // MAC地址
      byte[] macBytes = networkInterface.getHardwareAddress();
      if (macBytes != null) {
        StringBuilder macAddress = new StringBuilder();
        for (int i = 0; i < macBytes.length; i++) {
          macAddress.append(String.format("%02x%s", macBytes[i], (i < macBytes.length - 1) ? ":" : ""));
        }
        interfaceInfo.setMacAddress(macAddress.toString());
      }

      // MTU
      interfaceInfo.setMtu(networkInterface.getMTU());
    } catch (Exception e) {
      // 如果基本信息获取失败，设置默认值
      interfaceInfo.setName(networkInterface.getName());
      interfaceInfo.setDisplayName("unknown");
      interfaceInfo.setUp(false);
      interfaceInfo.setLoopback(false);
      interfaceInfo.setVirtual(false);
      interfaceInfo.setPointToPoint(false);
      interfaceInfo.setSupportsMulticast(false);
      interfaceInfo.setMtu(0);
    }

    // IP地址信息（避免DNS解析）
    Map<String, NetworkMetricsResponse.AddressInfo> addressesInfo = new HashMap<>();
    List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
    for (int i = 0; i < interfaceAddresses.size(); i++) {
      InterfaceAddress address = interfaceAddresses.get(i);
      NetworkMetricsResponse.AddressInfo addressInfo = new NetworkMetricsResponse.AddressInfo();

      addressInfo.setAddress(address.getAddress().getHostAddress());
      // 避免DNS反向解析，直接使用IP地址作为主机名
      addressInfo.setHostName(address.getAddress().getHostAddress());
      addressInfo.setNetworkPrefixLength(address.getNetworkPrefixLength());

      if (address.getBroadcast() != null) {
        addressInfo.setBroadcast(address.getBroadcast().getHostAddress());
      }

      addressesInfo.put("address_" + i, addressInfo);
    }
    interfaceInfo.setAddresses(addressesInfo);

    // 父接口信息
    if (networkInterface.getParent() != null) {
      interfaceInfo.setParent(networkInterface.getParent().getName());
    }

    // 子接口（简化处理，只获取名称）
    Map<String, String> subInterfaces = new HashMap<>();
    try {
      Enumeration<NetworkInterface> subInterfaceEnum = networkInterface.getSubInterfaces();
      int index = 0;
      while (subInterfaceEnum.hasMoreElements() && index < 10) { // 限制子接口数量，避免过多
        NetworkInterface subInterface = subInterfaceEnum.nextElement();
        subInterfaces.put("sub_" + index, subInterface.getName());
        index++;
      }
    } catch (Exception e) {
      // 忽略子接口获取失败
    }
    interfaceInfo.setSubInterfaces(subInterfaces);

    return interfaceInfo;
  }

  private NetworkMetricsResponse.LocalhostInfo getLocalhostInfo() {
    NetworkMetricsResponse.LocalhostInfo localInfo = new NetworkMetricsResponse.LocalhostInfo();

    try {
      InetAddress localhost = InetAddress.getLocalHost();
      localInfo.setHostAddress(localhost.getHostAddress());
      localInfo.setLoopbackAddress(localhost.isLoopbackAddress());
      localInfo.setLinkLocalAddress(localhost.isLinkLocalAddress());
      localInfo.setSiteLocalAddress(localhost.isSiteLocalAddress());
      localInfo.setMulticastAddress(localhost.isMulticastAddress());

      // 避免DNS解析，使用IP地址作为主机名
      localInfo.setHostName(localhost.getHostAddress());
      localInfo.setCanonicalHostName(localhost.getHostAddress());

    } catch (Exception e) {
      // 如果获取失败，提供默认值
      localInfo.setHostName("unknown");
      localInfo.setHostAddress("127.0.0.1");
      localInfo.setCanonicalHostName("127.0.0.1");
      localInfo.setLoopbackAddress(true);
      localInfo.setLinkLocalAddress(false);
      localInfo.setSiteLocalAddress(false);
      localInfo.setMulticastAddress(false);
    }

    return localInfo;
  }

  public SystemSummaryResponse getSystemSummary() {
    try {
      SystemSummaryResponse summary = new SystemSummaryResponse();

      // 系统基本信息
      RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
      OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

      // 系统概览
      summary.setSystemTime(System.currentTimeMillis());
      summary.setUptime(runtimeBean.getUptime());
      summary.setAvailableProcessors(osBean.getAvailableProcessors());
      summary.setOsName(osBean.getName());
      summary.setOsVersion(osBean.getVersion());
      summary.setOsArch(osBean.getArch());

      // 内存概览
      MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
      summary.setHeapUsedMB(heapUsage.getUsed() / 1024 / 1024);
      summary.setHeapMaxMB(heapUsage.getMax() / 1024 / 1024);
      summary.setHeapUsagePercentage(
        Double.parseDouble(decimalFormat.format((double) heapUsage.getUsed() / heapUsage.getCommitted() * 100)));

      // 线程概览
      summary.setThreadCount(threadBean.getThreadCount());
      summary.setPeakThreadCount(threadBean.getPeakThreadCount());
      summary.setDaemonThreadCount(threadBean.getDaemonThreadCount());

      // 磁盘概览
      File root = File.listRoots()[0];
      summary.setDiskTotalSpaceGB(
        Double.parseDouble(decimalFormat.format(root.getTotalSpace() / (1024.0 * 1024.0 * 1024.0))));
      summary.setDiskUsableSpaceGB(
        Double.parseDouble(decimalFormat.format(root.getUsableSpace() / (1024.0 * 1024.0 * 1024.0))));
      summary.setDiskFreeSpaceGB(
        Double.parseDouble(decimalFormat.format(root.getFreeSpace() / (1024.0 * 1024.0 * 1024.0))));

      if (root.getTotalSpace() > 0) {
        double diskUsagePercentage = (double) (root.getTotalSpace() - root.getUsableSpace()) / root.getTotalSpace() * 100;
        summary.setDiskUsagePercentage(
          Double.parseDouble(decimalFormat.format(diskUsagePercentage)));
      } else {
        summary.setDiskUsagePercentage(0.0);
      }

      // 网络概览
      try {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        int activeInterfaces = 0;
        int upInterfaces = 0;

        while (networkInterfaces.hasMoreElements()) {
          NetworkInterface networkInterface = networkInterfaces.nextElement();
          activeInterfaces++;
          if (networkInterface.isUp()) {
            upInterfaces++;
          }
        }

        summary.setTotalNetworkInterfaces(activeInterfaces);
        summary.setUpNetworkInterfaces(upInterfaces);

        // 获取本机IP
        InetAddress localhost = InetAddress.getLocalHost();
        summary.setHostName(localhost.getHostName());
        summary.setHostAddress(localhost.getHostAddress());
      } catch (Exception e) {
        summary.setNetworkError("无法获取网络信息: " + e.getMessage());
      }

      // JVM概览
      summary.setJvmName(runtimeBean.getVmName());
      summary.setJvmVersion(runtimeBean.getVmVersion());
      summary.setJvmVendor(runtimeBean.getVmVendor());

      // 类加载概览
      ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
      summary.setLoadedClassCount(classBean.getLoadedClassCount());
      summary.setTotalLoadedClassCount(classBean.getTotalLoadedClassCount());

      // 垃圾回收概览
      List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
      long totalGcCount = 0;
      long totalGcTime = 0;
      for (GarbageCollectorMXBean gcBean : gcBeans) {
        totalGcCount += gcBean.getCollectionCount();
        totalGcTime += gcBean.getCollectionTime();
      }
      summary.setTotalGcCount(totalGcCount);
      summary.setTotalGcTime(totalGcTime);

      // CPU使用率（如果可用）
      if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
        com.sun.management.OperatingSystemMXBean sunOsBean =
          (com.sun.management.OperatingSystemMXBean) osBean;
        try {
          summary.setSystemCpuLoad(
            Double.parseDouble(decimalFormat.format(sunOsBean.getSystemCpuLoad() * 100)));
          summary.setProcessCpuLoad(
            Double.parseDouble(decimalFormat.format(sunOsBean.getProcessCpuLoad() * 100)));
        } catch (Exception e) {
          summary.setSystemCpuLoad(0.0);
          summary.setProcessCpuLoad(0.0);
        }
      }

      return summary;
    } catch (Exception e) {
      throw new RuntimeException("获取系统摘要失败: " + e.getMessage());
    }
  }

  public PrometheusMetricsResponse getPrometheusMetrics() {
    try {
      // 使用Quarkus内置的Prometheus端点
      // 这里返回一个简单的指标格式，实际使用中建议直接使用Quarkus的/metrics端点
      StringBuilder metrics = new StringBuilder();

      // 添加自定义指标
      addCustomMetrics(metrics);

      return PrometheusMetricsResponse.success(metrics.toString());
    } catch (Exception e) {
      throw new RuntimeException("获取Prometheus指标失败: " + e.getMessage());
    }
  }

  private void addCustomMetrics(StringBuilder metrics) {
    try {
      // 添加JVM指标
      RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
      ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();

      metrics.append("# HELP jvm_uptime_seconds JVM uptime in seconds\n");
      metrics.append("# TYPE jvm_uptime_seconds gauge\n");
      metrics.append("jvm_uptime_seconds ").append(runtimeBean.getUptime() / 1000.0).append("\n");

      metrics.append("# HELP jvm_classes_loaded Number of classes loaded\n");
      metrics.append("# TYPE jvm_classes_loaded gauge\n");
      metrics.append("jvm_classes_loaded ").append(classBean.getLoadedClassCount()).append("\n");

      // 添加内存指标
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

      metrics.append("# HELP jvm_memory_heap_used_bytes Heap memory used in bytes\n");
      metrics.append("# TYPE jvm_memory_heap_used_bytes gauge\n");
      metrics.append("jvm_memory_heap_used_bytes ").append(heapUsage.getUsed()).append("\n");

      metrics.append("# HELP jvm_memory_heap_max_bytes Maximum heap memory in bytes\n");
      metrics.append("# TYPE jvm_memory_heap_max_bytes gauge\n");
      metrics.append("jvm_memory_heap_max_bytes ").append(heapUsage.getMax()).append("\n");

      // 添加线程指标
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

      metrics.append("# HELP jvm_threads_current Current number of threads\n");
      metrics.append("# TYPE jvm_threads_current gauge\n");
      metrics.append("jvm_threads_current ").append(threadBean.getThreadCount()).append("\n");

      metrics.append("# HELP jvm_threads_peak Peak number of threads\n");
      metrics.append("# TYPE jvm_threads_peak gauge\n");
      metrics.append("jvm_threads_peak ").append(threadBean.getPeakThreadCount()).append("\n");

      // 添加系统指标
      OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

      metrics.append("# HELP system_cpu_available_processors Number of available processors\n");
      metrics.append("# TYPE system_cpu_available_processors gauge\n");
      metrics.append("system_cpu_available_processors ").append(osBean.getAvailableProcessors()).append("\n");

      metrics.append("# HELP system_load_average System load average\n");
      metrics.append("# TYPE system_load_average gauge\n");
      metrics.append("system_load_average ").append(osBean.getSystemLoadAverage()).append("\n");

      // 添加磁盘指标
      File root = File.listRoots()[0];
      metrics.append("# HELP disk_total_bytes Total disk space in bytes\n");
      metrics.append("# TYPE disk_total_bytes gauge\n");
      metrics.append("disk_total_bytes ").append(root.getTotalSpace()).append("\n");

      metrics.append("# HELP disk_free_bytes Free disk space in bytes\n");
      metrics.append("# TYPE disk_free_bytes gauge\n");
      metrics.append("disk_free_bytes ").append(root.getFreeSpace()).append("\n");

      metrics.append("# HELP disk_usable_bytes Usable disk space in bytes\n");
      metrics.append("# TYPE disk_usable_bytes gauge\n");
      metrics.append("disk_usable_bytes ").append(root.getUsableSpace()).append("\n");

      metrics.append("# HELP disk_used_bytes Used disk space in bytes\n");
      metrics.append("# TYPE disk_used_bytes gauge\n");
      metrics.append("disk_used_bytes ").append(root.getTotalSpace() - root.getUsableSpace()).append("\n");

      // 磁盘使用率
      if (root.getTotalSpace() > 0) {
        double diskUsagePercentage = (double) (root.getTotalSpace() - root.getUsableSpace()) / root.getTotalSpace() * 100;
        metrics.append("# HELP disk_usage_percentage Disk usage percentage\n");
        metrics.append("# TYPE disk_usage_percentage gauge\n");
        metrics.append("disk_usage_percentage ").append(diskUsagePercentage).append("\n");
      }

      // 网络指标
      try {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        int activeInterfaces = 0;
        int upInterfaces = 0;

        while (networkInterfaces.hasMoreElements()) {
          NetworkInterface networkInterface = networkInterfaces.nextElement();
          activeInterfaces++;
          if (networkInterface.isUp()) {
            upInterfaces++;
          }
        }

        metrics.append("# HELP network_interfaces_total Total number of network interfaces\n");
        metrics.append("# TYPE network_interfaces_total gauge\n");
        metrics.append("network_interfaces_total ").append(activeInterfaces).append("\n");

        metrics.append("# HELP network_interfaces_up Number of up network interfaces\n");
        metrics.append("# TYPE network_interfaces_up gauge\n");
        metrics.append("network_interfaces_up ").append(upInterfaces).append("\n");
      } catch (Exception e) {
        metrics.append("# ERROR: Cannot get network info: ").append(e.getMessage()).append("\n");
      }

    } catch (Exception e) {
      metrics.append("# ERROR: ").append(e.getMessage()).append("\n");
    }
  }

  public AllMetricsResponse getAllMetrics() {
    long startTime = System.currentTimeMillis();
    try {
      String projectId = SessionContextHolder.getProjectId();
      // 并行获取所有指标
      CompletableFuture<FmMetricsResponse> fmFuture = CompletableFuture.supplyAsync(() -> {
        SessionContextHolder.setProjectId(projectId);
        return getFmMetrics(projectId);
      });

      CompletableFuture<JvmMetricsResponse> jvmFuture =
        CompletableFuture.supplyAsync(this::getJvmMetrics);

      CompletableFuture<CpuMetricsResponse> cpuFuture =
        CompletableFuture.supplyAsync(this::getCpuMetrics);

      CompletableFuture<MemoryMetricsResponse> memoryFuture =
        CompletableFuture.supplyAsync(this::getMemoryMetrics);

      CompletableFuture<ThreadMetricsResponse> threadsFuture =
        CompletableFuture.supplyAsync(this::getThreadMetrics);

      CompletableFuture<DiskMetricsResponse> diskFuture =
        CompletableFuture.supplyAsync(this::getDiskMetrics);

      CompletableFuture<NetworkMetricsResponse> networkFuture =
        CompletableFuture.supplyAsync(this::getNetworkMetrics);

      CompletableFuture<SystemSummaryResponse> summaryFuture =
        CompletableFuture.supplyAsync(this::getSystemSummary);

      CompletableFuture<PrometheusMetricsResponse> prometheusFuture =
        CompletableFuture.supplyAsync(this::getPrometheusMetrics);

      // 等待所有任务完成，设置超时时间为30秒
      CompletableFuture<Void> allFutures = CompletableFuture.allOf(
        fmFuture, jvmFuture, cpuFuture, memoryFuture, threadsFuture,
        diskFuture, networkFuture, summaryFuture, prometheusFuture
      );

      // 等待所有任务完成或超时
      allFutures.get(30, TimeUnit.SECONDS);

      // 获取结果
      FmMetricsResponse fm = fmFuture.get();
      JvmMetricsResponse jvm = jvmFuture.get();
      CpuMetricsResponse cpu = cpuFuture.get();
      MemoryMetricsResponse memory = memoryFuture.get();
      ThreadMetricsResponse threads = threadsFuture.get();
      DiskMetricsResponse disk = diskFuture.get();
      NetworkMetricsResponse network = networkFuture.get();
      SystemSummaryResponse summary = summaryFuture.get();
      PrometheusMetricsResponse prometheus = prometheusFuture.get();

      long processingTime = System.currentTimeMillis() - startTime;

      return AllMetricsResponse.builder()
        .fm(fm)
        .jvm(jvm)
        .cpu(cpu)
        .memory(memory)
        .threads(threads)
        .disk(disk)
        .network(network)
        .summary(summary)
        .prometheus(prometheus)
        .processingTimeMs(processingTime)
        .build();

    } catch (Exception e) {
      e.printStackTrace();
      long processingTime = System.currentTimeMillis() - startTime;
      String errorMessage = "获取监控指标失败: " + e.getMessage();
      log.error(errorMessage);
      AllMetricsResponse errorResponse = AllMetricsResponse.builder().error(errorMessage).build();
      errorResponse.setProcessingTimeMs(processingTime);
      return errorResponse;
    }
  }

}
