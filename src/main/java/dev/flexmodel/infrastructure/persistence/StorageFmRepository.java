package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Storage;
import dev.flexmodel.domain.model.storage.StorageRepository;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.session.Session;

import java.util.List;
import java.util.Optional;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class StorageFmRepository implements StorageRepository {

  @Inject
  Session session;

  @Override
  public List<Storage> findAll(String projectId) {
    return session.dsl()
      .selectFrom(Storage.class)
      .where(field(Storage::getProjectId).eq(projectId))
      .execute();
  }

  @Override
  public List<Storage> find(String projectId, Predicate filter) {
    return session.dsl()
      .selectFrom(Storage.class)
      .where(field(Storage::getProjectId).eq(projectId).and(filter))
      .execute();
  }

  @Override
  public Optional<Storage> findOne(String projectId, String name) {
    return session.dsl()
      .selectFrom(Storage.class)
      .where(field(Storage::getProjectId).eq(projectId).and(field(Storage::getName).eq(name)))
      .execute()
      .stream()
      .findFirst();
  }

  @Override
  public Storage save(Storage record) {

    session.dsl()
      .mergeInto(Storage.class)
      .values(record)
      .execute();

    return record;
  }

  @Override
  public void delete(String projectId, String name) {
    session.dsl().deleteFrom(Storage.class)
      .where(field(Storage::getProjectId).eq(projectId).and(field(Storage::getName).eq(name)))
      .execute();
  }

  @Override
  public Integer count(String projectId) {
    return (int) session.dsl().selectFrom(Storage.class)
      .where(field(Storage::getProjectId).eq(projectId))
      .count();
  }
}
