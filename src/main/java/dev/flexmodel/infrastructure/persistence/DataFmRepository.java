package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.codegen.StringUtils;
import dev.flexmodel.domain.model.data.DataRepository;
import dev.flexmodel.model.EntityDefinition;
import dev.flexmodel.model.field.TypedField;
import dev.flexmodel.query.DSLQueryBuilder;
import dev.flexmodel.query.Expressions;
import dev.flexmodel.query.Query;
import dev.flexmodel.session.Session;
import dev.flexmodel.session.SessionFactory;
import dev.flexmodel.shared.utils.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class DataFmRepository implements DataRepository {

  @Inject
  SessionFactory sessionFactory;

  @Override
  public List<Map<String, Object>> findRecords(String projectId,
                                               String datasourceName,
                                               String modelName,
                                               Integer page,
                                               Integer size,
                                               String filter,
                                               String sortString,
                                               boolean nestedQueryEnabled) {


    try (Session session = sessionFactory.createSession(datasourceName)) {

      DSLQueryBuilder queryBuilder = session.dsl()
        .selectFrom(modelName);

      if (!StringUtils.isBlank(filter)) {
        queryBuilder.where(filter);
      }

      if (size != null && page != null) {
        queryBuilder.page(page, size);
      }

      if (!StringUtils.isBlank(sortString)) {
        try {
          List<Query.OrderBy.Sort> sorts = JsonUtils.getInstance().parseToList(sortString, Query.OrderBy.Sort.class);
          Query.OrderBy sort = new Query.OrderBy();
          sort.getSorts().addAll(sorts);
          queryBuilder.orderBy(sort);
        } catch (Exception e) {
          log.error("Invalid sort string: {}", sortString, e);
        }
      }
      if (nestedQueryEnabled) {
        queryBuilder.enableNested();
      }
      return queryBuilder.execute();
    }
  }

  @Override
  public long countRecords(String projectId, String datasourceName, String modelName, String filter) {
    try (Session session = sessionFactory.createSession(datasourceName)) {

      DSLQueryBuilder queryBuilder = session.dsl()
        .selectFrom(modelName);

      if (!StringUtils.isBlank(filter)) {
        queryBuilder.where(filter);
      }

      return queryBuilder.count();
    }
  }

  @Override
  public Map<String, Object> findOneRecord(String projectId, String datasourceName, String modelName, Object id, boolean nestedQuery) {
    try (Session session = sessionFactory.createSession(datasourceName)) {

      DSLQueryBuilder queryBuilder = session.dsl()
        .selectFrom(modelName);

      EntityDefinition entity = (EntityDefinition) session.schema().getModel(modelName);
      Optional<TypedField<?, ?>> idField = entity.findIdField();

      return queryBuilder.where(Expressions.field(idField.orElseThrow().getName()).eq(id))
        .enableNested()
        .executeOne();
    }
  }

  @Override
  public Map<String, Object> createRecord(String projectId, String datasourceName, String modelName, Map<String, Object> data) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.dsl()
        .insertInto(modelName)
        .values(data)
        .execute();
      return data;
    }
  }

  @Override
  public Map<String, Object> updateRecord(String projectId, String datasourceName, String modelName, Object id, Map<String, Object> data) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      EntityDefinition entity = (EntityDefinition) session.schema().getModel(modelName);
      Optional<TypedField<?, ?>> idField = entity.findIdField();

      session.dsl()
        .update(modelName)
        .values(data)
        .where(Expressions.field(idField.orElseThrow().getName()).eq(id))
        .execute();

      return data;
    }
  }

  @Override
  public void deleteRecord(String projectId, String datasourceName, String modelName, Object id) {
    try (Session session = sessionFactory.createSession(datasourceName)) {

      EntityDefinition entity = (EntityDefinition) session.schema().getModel(modelName);
      Optional<TypedField<?, ?>> idField = entity.findIdField();

      session.dsl()
        .deleteFrom(modelName)
        .where(Expressions.field(idField.orElseThrow().getName()).eq(id))
        .execute();
    }
  }

}
