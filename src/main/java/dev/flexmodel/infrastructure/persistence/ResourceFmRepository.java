package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Resource;
import dev.flexmodel.domain.model.auth.ResourceRepository;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

@ApplicationScoped
public class ResourceFmRepository implements ResourceRepository {

  @Inject
  Session session;

  @Override
  public Resource findById(Long resourceId) {
    return session.dsl()
      .selectFrom(Resource.class)
      .where(field(Resource::getId).eq(resourceId))
      .executeOne();
  }

  @Override
  public List<Resource> findAll() {
    return session.dsl()
      .selectFrom(Resource.class)
      .execute();
  }

  @Override
  public Resource save(Resource resource) {
    session.dsl()
      .mergeInto(Resource.class)
      .values(resource)
      .execute();
    return resource;
  }

  @Override
  public void delete(Long resourceId) {
    session.dsl()
      .deleteFrom(Resource.class)
      .where(field(Resource::getId).eq(resourceId))
      .execute();
  }

  @Override
  public List<String> findPermissions(List<Long> resourceIds) {
    return session.dsl()
      .selectFrom(Resource.class)
      .where(field(Resource::getId).in(resourceIds))
      .execute().stream()
      .map(Resource::getPermission)
      .toList();

  }
}
