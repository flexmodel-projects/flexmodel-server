package tech.wetech.flexmodel.domain.model.storage;

import tech.wetech.flexmodel.codegen.entity.Storage;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;
import java.util.Optional;

/**
 * @author cjbi
 */
public interface StorageRepository {

  List<Storage> findAll(String projectId);

  List<Storage> find(String projectId, Predicate filter);

  Optional<Storage> findOne(String projectId, String name);

  Storage save(Storage storage);

  void delete(String projectId, String name);
}
