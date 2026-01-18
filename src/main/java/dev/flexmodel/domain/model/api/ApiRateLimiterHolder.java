package dev.flexmodel.domain.model.api;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author cjbi
 */
public class ApiRateLimiterHolder {

  @Getter
  private static final Map<String, ApiRateLimiter> map = new ConcurrentHashMap<>();

  public static ApiRateLimiter getApiRateLimiter(String apiId, int maxRequestCount, int intervalInSeconds) {
    return map.computeIfAbsent(apiId, k -> new ApiRateLimiter(maxRequestCount, intervalInSeconds));
  }

  public static void removeApiRateLimiter(String apiId) {
    map.remove(apiId);
  }

  public static void reset() {
    map.clear();
  }

  /**
   * @author cjbi
   */
  public static class ApiRateLimiter {

    private final int maxRequestCount;
    private final int intervalInSeconds;
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private long startTime;

    public ApiRateLimiter(int maxRequestCount, int intervalInSeconds) {
      this.maxRequestCount = maxRequestCount;
      this.intervalInSeconds = intervalInSeconds;
      this.startTime = System.currentTimeMillis();
    }

    public synchronized boolean tryAcquire() {
      long currentTime = System.currentTimeMillis();
      if ((currentTime - startTime) / 1000 > intervalInSeconds) {
        // 重置时间窗口和请求计数器
        startTime = currentTime;
        requestCounter.set(0);
      }

      // 检查当前时间窗口内的请求数是否超过限制
      if (requestCounter.get() < maxRequestCount) {
        requestCounter.incrementAndGet();
        return true;
      }
      return false;
    }
  }


}
