package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.domain.model.connect.DatasourceRepository;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DatasourceFmRepository implements DatasourceRepository {

  @Inject
  Session session;

  @Override
  public List<Datasource> findAll(String projectId) {
    return session.dsl()
      .selectFrom(Datasource.class)
      .where(field(Datasource::getProjectId).eq(projectId))
      .execute();
  }

  @Override
  public List<Datasource> find(String projectId, Predicate filter) {
    return session.dsl()
      .selectFrom(Datasource.class)
      .where(field(Datasource::getProjectId).eq(projectId).and(filter))
      .execute();
  }

  @Override
  public Datasource save(Datasource record) {

    session.dsl()
      .mergeInto(Datasource.class)
      .values(record)
      .execute();

    return record;
  }

  @Override
  public void delete(String projectId, String name) {
    session.dsl().deleteFrom(Datasource.class)
      .where(field(Datasource::getProjectId).eq(projectId).and(field(Datasource::getName).eq(name)))
      .execute();
  }

  @Override
  public Integer count(String projectId) {
    return (int) session.dsl().selectFrom(Datasource.class)
      .where(field(Datasource::getProjectId).eq(projectId))
      .count();
  }
}
