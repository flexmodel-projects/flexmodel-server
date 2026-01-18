package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.AiChatConversation;
import dev.flexmodel.domain.model.ai.AiChatConversationRepository;
import dev.flexmodel.query.Direction;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class AiChatConversationFmRepository implements AiChatConversationRepository {

  @Inject
  Session session;

  @Override
  public List<AiChatConversation> findAll(String projectId) {
    return session.dsl()
      .selectFrom(AiChatConversation.class)
      .where(field(AiChatConversation::getProjectId).eq(projectId))
      .orderBy("created_at", Direction.DESC)
      .execute();
  }

  @Override
  public List<AiChatConversation> find(String projectId, Predicate filter) {
    return session.dsl()
      .selectFrom(AiChatConversation.class)
      .where(field(AiChatConversation::getProjectId).eq(projectId).and(filter))
      .execute();
  }

  @Override
  public AiChatConversation findById(String projectId, String id) {
    return session.dsl()
      .selectFrom(AiChatConversation.class)
      .where(field(AiChatConversation::getProjectId).eq(projectId).and(field(AiChatConversation::getId).eq(id)))
      .executeOne();
  }

  @Override
  public AiChatConversation save(String projectId, AiChatConversation conversation) {
    session.dsl()
      .mergeInto(AiChatConversation.class)
      .values(conversation)
      .execute();

    return conversation;
  }

  @Override
  public void delete(String projectId, String id) {
    session.dsl().deleteFrom(AiChatConversation.class)
      .where(field(AiChatConversation::getProjectId).eq(projectId).and(field(AiChatConversation::getId).eq(id)))
      .execute();
  }
}
