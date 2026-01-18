package dev.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.AiChatConversation;
import dev.flexmodel.codegen.entity.AiChatMessage;
import dev.flexmodel.domain.model.ai.AiChatService;

import java.util.List;

@ApplicationScoped
public class AiApplicationService {

  @Inject
  AiChatService aiChatService;

  /**
   * 创建新的聊天会话
   */
  public AiChatConversation createConversation(String title) {
    return aiChatService.createConversation(title);
  }

  public AiChatMessage saveMessage(AiChatMessage message) {
    return aiChatService.saveMessage(message);
  }

  /**
   * 获取所有会话
   */
  public List<AiChatConversation> getAllConversations() {
    return aiChatService.getAllConversations();
  }

  /**
   * 根据ID获取会话
   */
  public AiChatConversation getConversationById(String conversationId) {
    return aiChatService.getConversationById(conversationId);
  }

  /**
   * 删除会话
   */
  public void deleteConversation(String conversationId) {
    aiChatService.deleteConversation(conversationId);
  }

  /**
   * 获取会话的所有消息
   */
  public List<AiChatMessage> getMessagesByConversationId(String conversationId) {
    return aiChatService.getMessagesByConversationId(conversationId);
  }


}
