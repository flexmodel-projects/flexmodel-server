package tech.wetech.flexmodel.interfaces.rest.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Prometheus指标响应DTO
 *
 * @author cjbi
 */
@Getter
@Setter
public class PrometheusMetricsResponse {

  private String metrics;

  private String error;

  public PrometheusMetricsResponse() {
  }

  public PrometheusMetricsResponse(String metrics, String error) {
    this.metrics = metrics;
    this.error = error;
  }

  public static PrometheusMetricsResponse success(String metrics) {
    return new PrometheusMetricsResponse(metrics, null);
  }

  public static PrometheusMetricsResponse error(String error) {
    return new PrometheusMetricsResponse(null, error);
  }
}
