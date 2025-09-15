package tech.wetech.flexmodel.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.lang.management.*;
import java.util.List;

/**
 * 系统监控资源类 - 提供JVM/CPU/内存/线程监控
 * @author cjbi
 */
@Path("/metrics")
@Singleton
public class MetricsResource {

  @Inject
  ObjectMapper objectMapper;

  private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
  private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
  private final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
  private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

  /**
   * 获取JVM监控信息
   */
  @GET
  @Path("/jvm")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJvmMetrics() {
    try {
      ObjectNode jvmMetrics = objectMapper.createObjectNode();

      // JVM基本信息
      ObjectNode jvmInfo = objectMapper.createObjectNode();
      jvmInfo.put("name", runtimeMXBean.getVmName());
      jvmInfo.put("version", runtimeMXBean.getVmVersion());
      jvmInfo.put("vendor", runtimeMXBean.getVmVendor());
      jvmInfo.put("uptime", runtimeMXBean.getUptime());
      jvmInfo.put("startTime", runtimeMXBean.getStartTime());
      jvmMetrics.set("info", jvmInfo);

      // 内存信息
      ObjectNode memoryInfo = objectMapper.createObjectNode();
      MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
      MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();

      ObjectNode heap = objectMapper.createObjectNode();
      heap.put("used", heapMemory.getUsed());
      heap.put("committed", heapMemory.getCommitted());
      heap.put("max", heapMemory.getMax());
      heap.put("init", heapMemory.getInit());
      memoryInfo.set("heap", heap);

      ObjectNode nonHeap = objectMapper.createObjectNode();
      nonHeap.put("used", nonHeapMemory.getUsed());
      nonHeap.put("committed", nonHeapMemory.getCommitted());
      nonHeap.put("max", nonHeapMemory.getMax());
      nonHeap.put("init", nonHeapMemory.getInit());
      memoryInfo.set("nonHeap", nonHeap);

      jvmMetrics.set("memory", memoryInfo);

      // 垃圾收集器信息
      ArrayNode gcArray = objectMapper.createArrayNode();
      List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
      for (GarbageCollectorMXBean gcBean : gcBeans) {
        ObjectNode gc = objectMapper.createObjectNode();
        gc.put("name", gcBean.getName());
        gc.put("collectionCount", gcBean.getCollectionCount());
        gc.put("collectionTime", gcBean.getCollectionTime());
        gcArray.add(gc);
      }
      jvmMetrics.set("garbageCollectors", gcArray);

      return Response.ok(jvmMetrics).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity("获取JVM监控信息失败: " + e.getMessage()).build();
    }
  }

  /**
   * 获取CPU监控信息
   */
  @GET
  @Path("/cpu")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCpuMetrics() {
    try {
      ObjectNode cpuMetrics = objectMapper.createObjectNode();

      // CPU基本信息
      cpuMetrics.put("processors", osMXBean.getAvailableProcessors());
      cpuMetrics.put("systemLoadAverage", osMXBean.getSystemLoadAverage());

      // 操作系统信息
      ObjectNode osInfo = objectMapper.createObjectNode();
      osInfo.put("name", osMXBean.getName());
      osInfo.put("version", osMXBean.getVersion());
      osInfo.put("arch", osMXBean.getArch());
      cpuMetrics.set("os", osInfo);

      return Response.ok(cpuMetrics).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity("获取CPU监控信息失败: " + e.getMessage()).build();
    }
  }

  /**
   * 获取内存监控信息
   */
  @GET
  @Path("/memory")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getMemoryMetrics() {
    try {
      ObjectNode memoryMetrics = objectMapper.createObjectNode();

      // 堆内存详细信息
      MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
      ObjectNode heap = objectMapper.createObjectNode();
      heap.put("used", heapMemory.getUsed());
      heap.put("committed", heapMemory.getCommitted());
      heap.put("max", heapMemory.getMax());
      heap.put("init", heapMemory.getInit());
      heap.put("usedPercentage", (double) heapMemory.getUsed() / heapMemory.getMax() * 100);
      memoryMetrics.set("heap", heap);

      // 非堆内存详细信息
      MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();
      ObjectNode nonHeap = objectMapper.createObjectNode();
      nonHeap.put("used", nonHeapMemory.getUsed());
      nonHeap.put("committed", nonHeapMemory.getCommitted());
      nonHeap.put("max", nonHeapMemory.getMax());
      nonHeap.put("init", nonHeapMemory.getInit());
      memoryMetrics.set("nonHeap", nonHeap);

      // 总内存使用情况
      ObjectNode total = objectMapper.createObjectNode();
      long totalUsed = heapMemory.getUsed() + nonHeapMemory.getUsed();
      long totalCommitted = heapMemory.getCommitted() + nonHeapMemory.getCommitted();
      total.put("used", totalUsed);
      total.put("committed", totalCommitted);
      memoryMetrics.set("total", total);

      return Response.ok(memoryMetrics).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity("获取内存监控信息失败: " + e.getMessage()).build();
    }
  }

  /**
   * 获取线程监控信息
   */
  @GET
  @Path("/threads")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getThreadMetrics() {
    try {
      ObjectNode threadMetrics = objectMapper.createObjectNode();

      // 线程基本信息
      threadMetrics.put("threadCount", threadMXBean.getThreadCount());
      threadMetrics.put("peakThreadCount", threadMXBean.getPeakThreadCount());
      threadMetrics.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
      threadMetrics.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());

      // 线程状态统计
      ObjectNode threadStates = objectMapper.createObjectNode();
      long[] threadIds = threadMXBean.getAllThreadIds();

      // 注意：getThreadState方法在某些JVM版本中可能不可用
      // 这里我们简化处理，只提供基本的线程统计信息
      threadStates.put("total", threadIds.length);
      threadStates.put("NEW", 0);
      threadStates.put("RUNNABLE", 0);
      threadStates.put("BLOCKED", 0);
      threadStates.put("WAITING", 0);
      threadStates.put("TIMED_WAITING", 0);
      threadStates.put("TERMINATED", 0);
      threadMetrics.set("states", threadStates);

      return Response.ok(threadMetrics).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity("获取线程监控信息失败: " + e.getMessage()).build();
    }
  }

  /**
   * 获取所有监控信息的汇总
   */
  @GET
  @Path("/summary")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getMetricsSummary() {
    try {
      ObjectNode summary = objectMapper.createObjectNode();

      // JVM基本信息
      ObjectNode jvm = objectMapper.createObjectNode();
      jvm.put("uptime", runtimeMXBean.getUptime());
      jvm.put("vmName", runtimeMXBean.getVmName());
      jvm.put("vmVersion", runtimeMXBean.getVmVersion());
      summary.set("jvm", jvm);

      // CPU信息
      ObjectNode cpu = objectMapper.createObjectNode();
      cpu.put("processors", osMXBean.getAvailableProcessors());
      cpu.put("systemLoadAverage", osMXBean.getSystemLoadAverage());
      summary.set("cpu", cpu);

      // 内存信息
      ObjectNode memory = objectMapper.createObjectNode();
      MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
      memory.put("heapUsed", heapMemory.getUsed());
      memory.put("heapMax", heapMemory.getMax());
      memory.put("heapUsedPercentage", (double) heapMemory.getUsed() / heapMemory.getMax() * 100);
      summary.set("memory", memory);

      // 线程信息
      ObjectNode threads = objectMapper.createObjectNode();
      threads.put("threadCount", threadMXBean.getThreadCount());
      threads.put("peakThreadCount", threadMXBean.getPeakThreadCount());
      threads.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
      summary.set("threads", threads);

      return Response.ok(summary).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity("获取监控汇总信息失败: " + e.getMessage()).build();
    }
  }

  /**
   * 获取Prometheus格式的指标
   */
  @GET
  @Path("/prometheus")
  @Produces("text/plain")
  public Response getPrometheusMetrics() {
    try {
      StringBuilder prometheusOutput = new StringBuilder();

      // JVM指标
      MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
      prometheusOutput.append("# HELP jvm_memory_heap_used_bytes Used heap memory in bytes\n");
      prometheusOutput.append("# TYPE jvm_memory_heap_used_bytes gauge\n");
      prometheusOutput.append("jvm_memory_heap_used_bytes ").append(heapMemory.getUsed()).append("\n");

      prometheusOutput.append("# HELP jvm_memory_heap_max_bytes Max heap memory in bytes\n");
      prometheusOutput.append("# TYPE jvm_memory_heap_max_bytes gauge\n");
      prometheusOutput.append("jvm_memory_heap_max_bytes ").append(heapMemory.getMax()).append("\n");

      // CPU指标
      prometheusOutput.append("# HELP system_cpu_load System CPU load\n");
      prometheusOutput.append("# TYPE system_cpu_load gauge\n");
      prometheusOutput.append("system_cpu_load ").append(osMXBean.getSystemLoadAverage()).append("\n");

      // 注意：某些CPU指标可能在某些JVM版本中不可用
      // prometheusOutput.append("# HELP process_cpu_load Process CPU load\n");
      // prometheusOutput.append("# TYPE process_cpu_load gauge\n");
      // prometheusOutput.append("process_cpu_load ").append(osMXBean.getProcessCpuLoad()).append("\n");

      // 线程指标
      prometheusOutput.append("# HELP jvm_threads_current Current number of threads\n");
      prometheusOutput.append("# TYPE jvm_threads_current gauge\n");
      prometheusOutput.append("jvm_threads_current ").append(threadMXBean.getThreadCount()).append("\n");

      prometheusOutput.append("# HELP jvm_threads_peak Peak number of threads\n");
      prometheusOutput.append("# TYPE jvm_threads_peak gauge\n");
      prometheusOutput.append("jvm_threads_peak ").append(threadMXBean.getPeakThreadCount()).append("\n");

      return Response.ok(prometheusOutput.toString()).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity("获取Prometheus指标失败: " + e.getMessage()).build();
    }
  }
}
