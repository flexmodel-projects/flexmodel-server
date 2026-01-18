package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 网络监控响应DTO
 *
 * @author cjbi
 */
@Getter
@Setter
public class NetworkMetricsResponse {

  private Map<String, NetworkInterfaceInfo> interfaces;

  private int totalInterfaces;

  private LocalhostInfo localhost;

  private NetworkStatsInfo stats;

  private String localhostError;

  @Getter
  @Setter
  public static class NetworkInterfaceInfo {
    private String name;

    private String displayName;

    private boolean isUp;

    private boolean isLoopback;

    private boolean isVirtual;

    private boolean isPointToPoint;

    private boolean supportsMulticast;

    private String macAddress;

    private int mtu;

    private Map<String, AddressInfo> addresses;

    private String parent;

    private Map<String, String> subInterfaces;

  }

  @Getter
  @Setter
  public static class AddressInfo {
    private String address;

    private String hostName;

    private short networkPrefixLength;

    private String broadcast;

  }

  @Getter
  @Setter
  public static class LocalhostInfo {
    private String hostName;

    private String hostAddress;

    private String canonicalHostName;

    private boolean isLoopbackAddress;

    private boolean isLinkLocalAddress;

    private boolean isSiteLocalAddress;

    private boolean isMulticastAddress;

  }

  @Getter
  @Setter
  public static class NetworkStatsInfo {
    private int activeInterfaces;

    private long timestamp;

  }
}
