package dev.flexmodel;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

/**
 * @author cjbi
 */
public class SQLiteTestResource implements QuarkusTestResourceLifecycleManager {

  @Override
  public Map<String, String> start() {
    // 使用内存SQLite数据库进行测试
    return Map.of(
      "flexmodel.datasource.db-kind", "sqlite",
      "flexmodel.datasource.url", "jdbc:sqlite:file::memory:?cache=shared",
//      "flexmodel.datasource.url", "jdbc:sqlite:file:sqlite.db",
      "flexmodel.datasource.username", "",
      "flexmodel.datasource.password", "",
      "SQLITE_URL", "jdbc:sqlite:file::memory:?cache=shared",
//      "SQLITE_URL", "jdbc:sqlite:file:sqlite.db",
      "SQLITE_USERNAME", "",
      "SQLITE_PASSWORD", "");
  }

  @Override
  public void stop() {
    // SQLite内存数据库会在JVM关闭时自动清理，无需手动停止
  }
}
