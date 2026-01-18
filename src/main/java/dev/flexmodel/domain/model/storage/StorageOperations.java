package dev.flexmodel.domain.model.storage;

import dev.flexmodel.application.dto.FileItem;

import java.io.InputStream;
import java.util.List;

/**
 * 存储操作接口
 * @author cjbi
 */
public interface StorageOperations {

  List<FileItem> listFiles(String path);

  FileItem getFile(String path);

  void uploadFile(String path, InputStream inputStream, long size);

  void deleteFile(String path);

  void createFolder(String path);

  boolean exists(String path);

  long getFileSize(String path);
}
