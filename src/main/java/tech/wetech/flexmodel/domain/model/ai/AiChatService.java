package tech.wetech.flexmodel.domain.model.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.AiChatConversation;
import tech.wetech.flexmodel.codegen.entity.AiChatMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author cjbi
 */
@ApplicationScoped
public class AiChatService {

  @Inject
  AiChatConversationRepository conversationRepository;

  @Inject
  AiChatMessageRepository messageRepository;

  /**
   * 创建新的聊天会话
   */
  public AiChatConversation createConversation(String title) {
    AiChatConversation conversation = new AiChatConversation();
    conversation.setId(UUID.randomUUID().toString());
    conversation.setTitle(title);
    conversation.setCreatedAt(LocalDateTime.now());
    
    return conversationRepository.save(conversation);
  }

  /**
   * 获取所有会话
   */
  public List<AiChatConversation> getAllConversations() {
    return conversationRepository.findAll();
  }

  /**
   * 根据ID获取会话
   */
  public AiChatConversation getConversationById(String conversationId) {
    return conversationRepository.findById(conversationId);
  }

  /**
   * 删除会话
   */
  public void deleteConversation(String conversationId) {
    // 先删除会话下的所有消息
    messageRepository.deleteByConversationId(conversationId);
    // 再删除会话
    conversationRepository.delete(conversationId);
  }

  /**
   * 发送消息
   */
  public AiChatMessage sendMessage(String conversationId, String role, String content) {
    AiChatMessage message = new AiChatMessage();
    message.setId(UUID.randomUUID().toString());
    message.setConversationId(conversationId);
    message.setRole(role);
    message.setContent(content);
    message.setCreatedAt(LocalDateTime.now());
    
    return messageRepository.save(message);
  }

  /**
   * 获取会话的所有消息
   */
  public List<AiChatMessage> getMessagesByConversationId(String conversationId) {
    return messageRepository.findByConversationId(conversationId);
  }

  /**
   * 删除消息
   */
  public void deleteMessage(String messageId) {
    messageRepository.delete(messageId);
  }
}
