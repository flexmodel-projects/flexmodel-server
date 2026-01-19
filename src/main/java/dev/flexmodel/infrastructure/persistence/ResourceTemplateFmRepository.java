package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.ResourceTemplate;
import dev.flexmodel.domain.model.auth.ResourceTemplateRepository;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

@ApplicationScoped
public class ResourceTemplateFmRepository implements ResourceTemplateRepository {

  @Inject
  Session session;

  @Override
  public ResourceTemplate findById(Long templateId) {
    return session.dsl()
      .selectFrom(ResourceTemplate.class)
      .where(field(ResourceTemplate::getId).eq(templateId))
      .executeOne();
  }

  @Override
  public List<ResourceTemplate> findAll() {
    return session.dsl()
      .selectFrom(ResourceTemplate.class)
      .execute();
  }

  @Override
  public ResourceTemplate save(ResourceTemplate resourceTemplate) {
    session.dsl()
      .mergeInto(ResourceTemplate.class)
      .values(resourceTemplate)
      .execute();
    return resourceTemplate;
  }

  @Override
  public void delete(Long templateId) {
    session.dsl()
      .deleteFrom(ResourceTemplate.class)
      .where(field(ResourceTemplate::getId).eq(templateId))
      .execute();
  }
}
