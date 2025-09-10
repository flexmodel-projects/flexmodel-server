package tech.wetech.flexmodel;

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
      "flexmodel.datasource.db-kind", "mysql",
      "flexmodel.datasource.url", "jdbc:mysql://metacode.wetech.tech:3306/flexmodel?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&autoReconnect=true",
      "flexmodel.datasource.username", "root",
      "flexmodel.datasource.password", "metacode@123!",
      "SQLITE_URL", "jdbc:mysql://metacode.wetech.tech:3306/flexmodel?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&autoReconnect=true",
      "SQLITE_USERNAME", "root",
      "SQLITE_PASSWORD", "metacode@123!");
  }

  @Override
  public void stop() {
    // SQLite内存数据库会在JVM关闭时自动清理，无需手动停止
  }
}
