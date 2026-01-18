package dev.flexmodel.domain.model.ai;

import dev.flexmodel.codegen.entity.AiChatConversation;
import dev.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface AiChatConversationRepository {

  List<AiChatConversation> findAll(String projectId);

  List<AiChatConversation> find(String projectId, Predicate filter);

  AiChatConversation findById(String projectId, String id);

  AiChatConversation save(String projectId, AiChatConversation conversation);

  void delete(String projectId, String id);
}
