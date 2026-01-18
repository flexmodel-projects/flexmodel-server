package dev.flexmodel.interfaces.rest;

import dev.flexmodel.application.dto.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.MetricsApplicationService;
import dev.flexmodel.application.dto.*;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统监控资源类
 * 提供JVM、CPU、内存、线程、磁盘（含I/O）、网络等监控信息
 *
 * @author cjbi
 */
@Path("/v1/projects/{projectId}/metrics")
@Tag(name = "系统监控", description = "系统监控相关接口，包括JVM、CPU、内存、线程、磁盘（含I/O）、网络等监控信息")
@SecurityRequirement(name = "BearerAuth")
public class MetricsResource {
  @Inject
  MetricsApplicationService metricsApplicationService;


  @GET
  @Path("/fm")
  public FmMetricsResponse getFmMetrics(@PathParam("projectId") String projectId) {
    return metricsApplicationService.getFmMetrics(projectId);
  }

  /**
   * JVM监控信息
   */
  @GET
  @Path("/jvm")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "获取JVM监控信息",
    description = "返回JVM运行时的详细信息，包括类加载、垃圾回收、系统属性等"
  )
  @APIResponse(
    responseCode = "200",
    description = "成功获取JVM信息",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = JvmMetricsResponse.class)
    )
  )
  public JvmMetricsResponse getJvmMetrics() {
    return metricsApplicationService.getJvmMetrics();
  }

  /**
   * CPU监控信息
   */
  @GET
  @Path("/cpu")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "获取CPU监控信息",
    description = "返回CPU使用率、系统负载、物理内存等系统资源信息"
  )
  @APIResponse(
    responseCode = "200",
    description = "成功获取CPU信息",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = CpuMetricsResponse.class)
    )
  )
  public CpuMetricsResponse getCpuMetrics() {
    return metricsApplicationService.getCpuMetrics();
  }

  /**
   * 内存监控信息
   */
  @GET
  @Path("/memory")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "获取内存监控信息",
    description = "返回堆内存、非堆内存、内存池等详细内存使用情况"
  )
  @APIResponse(
    responseCode = "200",
    description = "成功获取内存信息",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = MemoryMetricsResponse.class)
    )
  )
  public MemoryMetricsResponse getMemoryMetrics() {
    return metricsApplicationService.getMemoryMetrics();
  }

  /**
   * 线程监控信息
   */
  @GET
  @Path("/threads")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "获取线程监控信息",
    description = "返回线程数量、状态统计、线程详细信息等"
  )
  @APIResponse(
    responseCode = "200",
    description = "成功获取线程信息",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = ThreadMetricsResponse.class)
    )
  )
  public ThreadMetricsResponse getThreadMetrics() {
    return metricsApplicationService.getThreadMetrics();
  }

  /**
   * 磁盘监控信息
   */
  @GET
  @Path("/disk")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "获取磁盘监控信息",
    description = "返回磁盘使用情况、文件系统信息、磁盘I/O统计等存储设备监控数据"
  )
  @APIResponse(
    responseCode = "200",
    description = "成功获取磁盘信息",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = DiskMetricsResponse.class)
    )
  )
  public DiskMetricsResponse getDiskMetrics() {
    return metricsApplicationService.getDiskMetrics();
  }

  /**
   * 网络监控信息
   */
  @GET
  @Path("/network")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "获取网络监控信息",
    description = "返回网络接口信息、连接状态、IP地址等网络监控数据"
  )
  @APIResponse(
    responseCode = "200",
    description = "成功获取网络信息",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = NetworkMetricsResponse.class)
    )
  )
  public NetworkMetricsResponse getNetworkMetrics() {
    return metricsApplicationService.getNetworkMetrics();
  }

  /**
   * 处理单个网络接口（优化版本）
   */
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

  /**
   * 系统摘要信息
   */
  @GET
  @Path("/summary")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "获取系统摘要信息",
    description = "返回系统运行状态的综合摘要，包括JVM、CPU、内存、线程等关键指标"
  )
  @APIResponse(
    responseCode = "200",
    description = "成功获取系统摘要",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = SystemSummaryResponse.class)
    )
  )
  public SystemSummaryResponse getSystemSummary() {
    return metricsApplicationService.getSystemSummary();
  }

  /**
   * Prometheus格式的指标导出
   */
  @GET
  @Path("/prometheus")
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(
    summary = "获取Prometheus格式指标",
    description = "返回Prometheus格式的监控指标数据，可用于Prometheus服务器采集"
  )
  @APIResponse(
    responseCode = "200",
    description = "成功获取Prometheus指标",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = PrometheusMetricsResponse.class)
    )
  )
  public PrometheusMetricsResponse getPrometheusMetrics() {
    return metricsApplicationService.getPrometheusMetrics();
  }

  /**
   * 获取所有监控指标
   */
  @GET
  @Path("/all")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "获取所有监控指标",
    description = "获取JVM、CPU、内存、线程、磁盘、网络等所有监控信息，提供系统完整状态"
  )
  @APIResponse(
    responseCode = "200",
    description = "成功获取所有监控指标",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = AllMetricsResponse.class)
    )
  )
  public AllMetricsResponse getAllMetrics() {
    return metricsApplicationService.getAllMetrics();
  }

}
