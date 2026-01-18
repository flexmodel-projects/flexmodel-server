package dev.flexmodel.domain.model.storage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.application.dto.FileItem;
import dev.flexmodel.codegen.entity.Storage;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class StorageService {

  @Inject
  StorageRepository storageRepository;

  public Storage createStorage(String projectId, Storage storage) {
    Optional<Storage> optional = findOne(projectId, storage.getName());
    if (optional.isPresent()) {
      throw new RuntimeException("The storage name is duplicated");
    }
    storage = storageRepository.save(storage);
    return storage;
  }

  public Storage updateStorage(String projectId, Storage storage) {
    Optional<Storage> optional = findOne(projectId, storage.getName());
    if (optional.isEmpty()) {
      return storage;
    }
    storage.setEnabled(optional.orElseThrow().getEnabled());
    storage.setType(optional.orElseThrow().getType());
    storage.setCreatedAt(optional.orElseThrow().getCreatedAt());
    storage = storageRepository.save(storage);
    return storage;
  }

  public List<Storage> findAll(String projectId) {
    return storageRepository.find(projectId, field(Storage::getEnabled).eq(true));
  }

  public Optional<Storage> findOne(String projectId, String storageName) {
    return storageRepository.find(projectId, field(Storage::getName).eq(storageName))
      .stream()
      .findFirst();
  }

  public void deleteStorage(String projectId, String storageName) {
    storageRepository.delete(projectId, storageName);
  }

  public List<FileItem> listFiles(String projectId, String storageName, String path) {
    Storage storage = findOne(projectId, storageName)
      .orElseThrow(() -> new RuntimeException("Storage not found: " + storageName));

    StorageOperations operations = StorageOperationsFactory.create(storage);
    return operations.listFiles(path);
  }

  public FileItem getFile(String projectId, String storageName, String path) {
    Storage storage = findOne(projectId, storageName)
      .orElseThrow(() -> new RuntimeException("Storage not found: " + storageName));

    StorageOperations operations = StorageOperationsFactory.create(storage);
    return operations.getFile(path);
  }

  public void uploadFile(String projectId, String storageName, String path, InputStream inputStream, long size) {
    Storage storage = findOne(projectId, storageName)
      .orElseThrow(() -> new RuntimeException("Storage not found: " + storageName));

    StorageOperations operations = StorageOperationsFactory.create(storage);
    operations.uploadFile(path, inputStream, size);
  }

  public void deleteFile(String projectId, String storageName, String path) {
    Storage storage = findOne(projectId, storageName)
      .orElseThrow(() -> new RuntimeException("Storage not found: " + storageName));

    StorageOperations operations = StorageOperationsFactory.create(storage);
    operations.deleteFile(path);
  }

  public void createFolder(String projectId, String storageName, String path) {
    Storage storage = findOne(projectId, storageName)
      .orElseThrow(() -> new RuntimeException("Storage not found: " + storageName));

    StorageOperations operations = StorageOperationsFactory.create(storage);
    operations.createFolder(path);
  }

  public boolean exists(String projectId, String storageName, String path) {
    Storage storage = findOne(projectId, storageName)
      .orElseThrow(() -> new RuntimeException("Storage not found: " + storageName));

    StorageOperations operations = StorageOperationsFactory.create(storage);
    return operations.exists(path);
  }

  public long getFileSize(String projectId, String storageName, String path) {
    Storage storage = findOne(projectId, storageName)
      .orElseThrow(() -> new RuntimeException("Storage not found: " + storageName));

    StorageOperations operations = StorageOperationsFactory.create(storage);
    return operations.getFileSize(path);
  }

  public Integer count(String projectId) {
    return storageRepository.count(projectId);
  }
}
