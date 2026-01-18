package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.AiChatMessage;
import dev.flexmodel.domain.model.ai.AiChatMessageRepository;
import dev.flexmodel.query.Direction;
import dev.flexmodel.query.Predicate;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class AiChatMessageFmRepository implements AiChatMessageRepository {

  @Inject
  Session session;

  @Override
  public List<AiChatMessage> findAll(String projectId) {
    return session.dsl()
      .selectFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getProjectId).eq(projectId))
      .execute();
  }

  @Override
  public List<AiChatMessage> find(String projectId, Predicate filter) {
    return session.dsl()
      .selectFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getProjectId).eq(projectId).and(filter))
      .execute();
  }

  @Override
  public List<AiChatMessage> findByConversationId(String projectId, String conversationId) {
    return session.dsl()
      .selectFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getProjectId).eq(projectId).and(field(AiChatMessage::getConversationId).eq(conversationId)))
      .orderBy("created_at", Direction.ASC)
      .execute();
  }

  @Override
  public AiChatMessage findById(String projectId, String id) {
    return session.dsl()
      .selectFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getProjectId).eq(projectId).and(field(AiChatMessage::getId).eq(id)))
      .executeOne();
  }

  @Override
  public AiChatMessage save(String projectId, AiChatMessage message) {
    session.dsl()
      .mergeInto(AiChatMessage.class)
      .values(message)
      .execute();

    return message;
  }

  @Override
  public void delete(String projectId, String id) {
    session.dsl().deleteFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getProjectId).eq(projectId).and(field(AiChatMessage::getId).eq(id)))
      .execute();
  }

  @Override
  public void deleteByConversationId(String projectId, String conversationId) {
    session.dsl().deleteFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getProjectId).eq(projectId).and(field(AiChatMessage::getConversationId).eq(conversationId)))
      .execute();
  }
}
