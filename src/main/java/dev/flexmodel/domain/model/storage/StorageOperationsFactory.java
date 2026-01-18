package dev.flexmodel.domain.model.storage;

import dev.flexmodel.codegen.entity.Storage;
import dev.flexmodel.codegen.enumeration.StorageType;
import dev.flexmodel.infrastructure.storage.LocalStorageOperations;
import dev.flexmodel.infrastructure.storage.S3StorageOperations;
import dev.flexmodel.shared.utils.JsonUtils;

import java.util.Map;

/**
 * 存储操作工厂类
 * @author cjbi
 */
@SuppressWarnings("all")
public class StorageOperationsFactory {

  public static StorageOperations create(Storage storage) {
    if (storage.getType() == StorageType.LOCAL) {
      return createLocalStorage(storage);
    } else if (storage.getType() == StorageType.S3) {
      return createS3Storage(storage);
    }
    throw new IllegalArgumentException("Unsupported storage type: " + storage.getType());
  }

  private static StorageOperations createLocalStorage(Storage storage) {
    Map<String, Object> config = JsonUtils.getInstance().convertValue(storage.getConfig(), Map.class);
    String basePath = (String) config.get("basePath");
    if (basePath == null || basePath.isEmpty()) {
      throw new IllegalArgumentException("Local storage config must include 'basePath'");
    }
    return new LocalStorageOperations(basePath);
  }

  private static StorageOperations createS3Storage(Storage storage) {
    Map<String, Object> config = JsonUtils.getInstance().convertValue(storage.getConfig(), Map.class);
    String accessKey = (String) config.get("accessKey");
    String secretKey = (String) config.get("secretKey");
    String bucket = (String) config.get("bucket");
    String region = (String) config.getOrDefault("region", "us-east-1");
    String endpoint = (String) config.get("endpoint");
    Boolean pathStyle = (Boolean) config.getOrDefault("pathStyle", false);

    if (accessKey == null || secretKey == null || bucket == null) {
      throw new IllegalArgumentException("S3 storage config must include 'accessKey', 'secretKey', and 'bucket'");
    }

    return new S3StorageOperations(accessKey, secretKey, bucket, region, endpoint, pathStyle);
  }
}
