package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionRepository;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDefinitionFmRepository implements ApiDefinitionRepository {

  @Inject
  Session session;

  @Override
  public void deleteByParentId(String parentId) {
    session.dsl()
      .deleteFrom(ApiDefinition.class)
      .where(field(ApiDefinition::getParentId).eq(parentId))
      .execute();
  }

  @Override
  public ApiDefinition findById(String id) {
    return session.dsl()
      .selectFrom(ApiDefinition.class)
      .where(field(ApiDefinition::getId).eq(id))
      .executeOne();
  }

  @Override
  public List<ApiDefinition> findAll() {
    return session.dsl()
      .selectFrom(ApiDefinition.class)
      .execute();
  }

  @Override
  public ApiDefinition save(ApiDefinition record) {
    session.dsl().mergeInto(ApiDefinition.class).values(record).execute();
    return record;
  }

  @Override
  public void delete(String id) {
    session.dsl().deleteFrom(ApiDefinition.class)
      .where(field(ApiDefinition::getId).eq(id))
      .execute();
  }

}
