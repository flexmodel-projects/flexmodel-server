package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.StringUtils;
import tech.wetech.flexmodel.domain.model.data.DataRepository;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.DSLQueryBuilder;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.query.expr.Expressions;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.util.JsonUtils;

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
  public List<Map<String, Object>> findRecords(String datasourceName,
                                               String modelName,
                                               Integer current,
                                               Integer pageSize,
                                               String filter,
                                               String sortString,
                                               boolean nestedQueryEnabled) {


    try (Session session = sessionFactory.createSession(datasourceName)) {

      DSLQueryBuilder queryBuilder = session.dsl()
        .select()
        .from(modelName);

      if (!StringUtils.isBlank(filter)) {
        queryBuilder.where(filter);
      }

      if (pageSize != null && current != null) {
        queryBuilder.page(current, pageSize);
      }

      if (!StringUtils.isBlank(sortString)) {
        try {
          List<Query.Sort.Order> orders = JsonUtils.getInstance().parseToList(sortString, Query.Sort.Order.class);
          Query.Sort sort = new Query.Sort();
          sort.getOrders().addAll(orders);
          queryBuilder.orderBy(sort);
        } catch (Exception e) {
          log.error("Invalid sort string: {}", sortString, e);
        }
      }
      return queryBuilder.enableNested()
        .execute();
    }
  }

  @Override
  public long countRecords(String datasourceName, String modelName, String filter) {
    try (Session session = sessionFactory.createSession(datasourceName)) {

      DSLQueryBuilder queryBuilder = session.dsl()
        .select()
        .from(modelName);

      if (!StringUtils.isBlank(filter)) {
        queryBuilder.where(filter);
      }

      return queryBuilder.count();
    }
  }

  @Override
  public Map<String, Object> findOneRecord(String datasourceName, String modelName, Object id, boolean nestedQuery) {
    try (Session session = sessionFactory.createSession(datasourceName)) {

      DSLQueryBuilder queryBuilder = session.dsl()
        .select()
        .from(modelName);

      EntityDefinition entity = (EntityDefinition) session.schema().getModel(modelName);
      Optional<TypedField<?, ?>> idField = entity.findIdField();

      return queryBuilder.where(Expressions.field(idField.orElseThrow().getName()).eq(id))
        .enableNested()
        .executeOne();
    }
  }

  @Override
  public Map<String, Object> createRecord(String datasourceName, String modelName, Map<String, Object> data) {
    try (Session session = sessionFactory.createSession(datasourceName)) {
      session.dsl()
        .insertInto(modelName)
        .values(data)
        .execute();
      return data;
    }
  }

  @Override
  public Map<String, Object> updateRecord(String datasourceName, String modelName, Object id, Map<String, Object> data) {
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
  public void deleteRecord(String datasourceName, String modelName, Object id) {
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
