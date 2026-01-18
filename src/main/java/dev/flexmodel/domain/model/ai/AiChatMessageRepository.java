package dev.flexmodel.domain.model.ai;

import dev.flexmodel.codegen.entity.AiChatMessage;
import dev.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface AiChatMessageRepository {

  List<AiChatMessage> findAll(String projectId);

  List<AiChatMessage> find(String projectId, Predicate filter);

  List<AiChatMessage> findByConversationId(String projectId, String conversationId);

  AiChatMessage findById(String projectId, String id);

  AiChatMessage save(String projectId, AiChatMessage message);

  void delete(String projectId, String id);

  void deleteByConversationId(String projectId, String conversationId);
}
