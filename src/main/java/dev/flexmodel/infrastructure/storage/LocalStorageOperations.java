package dev.flexmodel.infrastructure.storage;

import dev.flexmodel.application.dto.FileItem;
import dev.flexmodel.domain.model.storage.StorageOperations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 本地文件存储实现
 * @author cjbi
 */
public class LocalStorageOperations implements StorageOperations {

  private final Path basePath;

  public LocalStorageOperations(String basePath) {
    this.basePath = Paths.get(basePath).normalize();
    try {
      if (!Files.exists(this.basePath)) {
        Files.createDirectories(this.basePath);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to create base directory: " + basePath, e);
    }
  }

  @Override
  public List<FileItem> listFiles(String path) {
    List<FileItem> items = new ArrayList<>();
    Path targetPath = resolvePath(path);

    if (!Files.exists(targetPath)) {
      return items;
    }

    try (Stream<Path> stream = Files.list(targetPath)) {
      stream.forEach(p -> {
        try {
          BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
          String relativePath = basePath.relativize(p).toString();
          items.add(FileItem.builder()
            .name(p.getFileName().toString())
            .type(attrs.isDirectory() ? FileItem.FileType.folder : FileItem.FileType.file)
            .size(attrs.isDirectory() ? null : attrs.size())
            .lastModified(attrs.lastModifiedTime().toInstant())
            .path(relativePath)
            .build());
        } catch (IOException e) {
          throw new RuntimeException("Failed to read file attributes: " + p, e);
        }
      });
    } catch (IOException e) {
      throw new RuntimeException("Failed to list files: " + path, e);
    }

    return items;
  }

  @Override
  public FileItem getFile(String path) {
    Path targetPath = resolvePath(path);

    if (!Files.exists(targetPath)) {
      return null;
    }

    try {
      BasicFileAttributes attrs = Files.readAttributes(targetPath, BasicFileAttributes.class);
      String relativePath = basePath.relativize(targetPath).toString();
      return FileItem.builder()
        .name(targetPath.getFileName().toString())
        .type(attrs.isDirectory() ? FileItem.FileType.folder : FileItem.FileType.file)
        .size(attrs.isDirectory() ? null : attrs.size())
        .lastModified(attrs.lastModifiedTime().toInstant())
        .path(relativePath)
        .build();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get file info: " + path, e);
    }
  }

  @Override
  public void uploadFile(String path, InputStream inputStream, long size) {
    Path targetPath = resolvePath(path);

    try {
      Path parentDir = targetPath.getParent();
      if (parentDir != null && !Files.exists(parentDir)) {
        Files.createDirectories(parentDir);
      }

      Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Failed to upload file: " + path, e);
    }
  }

  @Override
  public void deleteFile(String path) {
    Path targetPath = resolvePath(path);

    try {
      if (Files.isDirectory(targetPath)) {
        Files.walkFileTree(targetPath, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
      } else {
        Files.deleteIfExists(targetPath);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete file: " + path, e);
    }
  }

  @Override
  public void createFolder(String path) {
    Path targetPath = resolvePath(path);

    try {
      Files.createDirectories(targetPath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create folder: " + path, e);
    }
  }

  @Override
  public boolean exists(String path) {
    return Files.exists(resolvePath(path));
  }

  @Override
  public long getFileSize(String path) {
    Path targetPath = resolvePath(path);

    try {
      if (Files.isDirectory(targetPath)) {
        return 0;
      }
      return Files.size(targetPath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to get file size: " + path, e);
    }
  }

  private Path resolvePath(String path) {
    path = path.startsWith("/") ? path.substring(1) : path;
    Path resolved = basePath.resolve(path).normalize();
    if (!resolved.startsWith(basePath)) {
      throw new SecurityException("Attempted to access path outside base directory: " + path);
    }
    return resolved;
  }
}
