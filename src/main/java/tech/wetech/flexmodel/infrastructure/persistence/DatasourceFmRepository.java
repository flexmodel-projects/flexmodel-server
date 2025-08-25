package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceRepository;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DatasourceFmRepository implements DatasourceRepository {

  @Inject
  Session session;

  @Override
  public List<Datasource> findAll() {
    return session.dsl()
      .selectFrom(Datasource.class)
      .execute();
  }

  @Override
  public List<Datasource> find(Predicate filter) {
    return session.dsl()
      .selectFrom(Datasource.class)
      .where(filter)
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
  public void delete(String id) {
    session.dsl().deleteFrom(Datasource.class)
      .where(field(Datasource::getName).eq(id))
      .execute();
  }
}
