package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Role;
import dev.flexmodel.domain.model.auth.RoleRepository;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

@ApplicationScoped
public class RoleFmRepository implements RoleRepository {

  @Inject
  Session session;

  @Override
  public Role findById(String roleId) {
    return session.dsl()
      .selectFrom(Role.class)
      .where(field(Role::getId).eq(roleId))
      .executeOne();
  }

  @Override
  public List<Role> findAll() {
    return session.dsl()
      .selectFrom(Role.class)
      .execute();
  }

  @Override
  public Role save(Role role) {
    session.dsl()
      .mergeInto(Role.class)
      .values(role)
      .execute();
    return role;
  }

  @Override
  public void delete(String roleId) {
    session.dsl()
      .deleteFrom(Role.class)
      .where(field(Role::getId).eq(roleId))
      .execute();
  }

  @Override
  public List<Role> findByIds(List<String> roleIds) {
    return session.dsl()
      .selectFrom(Role.class)
      .where(field(Role::getId).in(roleIds))
      .execute();

  }
}
