package dev.flexmodel.infrastructure.storage;

import dev.flexmodel.application.dto.FileItem;
import dev.flexmodel.domain.model.storage.StorageOperations;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * S3 文件存储实现（占位符实现，需要添加 AWS SDK 依赖）
 * @author cjbi
 */
public class S3StorageOperations implements StorageOperations {

  private final String accessKey;
  private final String secretKey;
  private final String bucket;
  private final String region;
  private final String endpoint;
  private final boolean pathStyle;

  public S3StorageOperations(String accessKey, String secretKey, String bucket, String region,
                            String endpoint, boolean pathStyle) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.bucket = bucket;
    this.region = region;
    this.endpoint = endpoint;
    this.pathStyle = pathStyle;
  }

  @Override
  public List<FileItem> listFiles(String path) {
    List<FileItem> items = new ArrayList<>();

    throw new UnsupportedOperationException("S3 storage operations require AWS SDK dependency. " +
      "Please add the following dependency to pom.xml:\n" +
      "<dependency>\n" +
      "  <groupId>software.amazon.awssdk</groupId>\n" +
      "  <artifactId>s3</artifactId>\n" +
      "</dependency>");
  }

  @Override
  public FileItem getFile(String path) {
    throw new UnsupportedOperationException("S3 storage operations require AWS SDK dependency. " +
      "Please add the following dependency to pom.xml:\n" +
      "<dependency>\n" +
      "  <groupId>software.amazon.awssdk</groupId>\n" +
      "  <artifactId>s3</artifactId>\n" +
      "</dependency>");
  }

  @Override
  public void uploadFile(String path, InputStream inputStream, long size) {
    throw new UnsupportedOperationException("S3 storage operations require AWS SDK dependency. " +
      "Please add the following dependency to pom.xml:\n" +
      "<dependency>\n" +
      "  <groupId>software.amazon.awssdk</groupId>\n" +
      "  <artifactId>s3</artifactId>\n" +
      "</dependency>");
  }

  @Override
  public void deleteFile(String path) {
    throw new UnsupportedOperationException("S3 storage operations require AWS SDK dependency. " +
      "Please add the following dependency to pom.xml:\n" +
      "<dependency>\n" +
      "  <groupId>software.amazon.awssdk</groupId>\n" +
      "  <artifactId>s3</artifactId>\n" +
      "</dependency>");
  }

  @Override
  public void createFolder(String path) {
    throw new UnsupportedOperationException("S3 storage operations require AWS SDK dependency. " +
      "Please add the following dependency to pom.xml:\n" +
      "<dependency>\n" +
      "  <groupId>software.amazon.awssdk</groupId>\n" +
      "  <artifactId>s3</artifactId>\n" +
      "</dependency>");
  }

  @Override
  public boolean exists(String path) {
    throw new UnsupportedOperationException("S3 storage operations require AWS SDK dependency. " +
      "Please add the following dependency to pom.xml:\n" +
      "<dependency>\n" +
      "  <groupId>software.amazon.awssdk</groupId>\n" +
      "  <artifactId>s3</artifactId>\n" +
      "</dependency>");
  }

  @Override
  public long getFileSize(String path) {
    throw new UnsupportedOperationException("S3 storage operations require AWS SDK dependency. " +
      "Please add the following dependency to pom.xml:\n" +
      "<dependency>\n" +
      "  <groupId>software.amazon.awssdk</groupId>\n" +
      "  <artifactId>s3</artifactId>\n" +
      "</dependency>");
  }
}
