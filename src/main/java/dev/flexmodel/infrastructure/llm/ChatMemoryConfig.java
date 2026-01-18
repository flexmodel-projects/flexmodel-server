package dev.flexmodel.infrastructure.llm;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ChatMemoryConfig {

  @Inject
  ChatMemoryStore chatMemoryStore;

  @Produces
  @Singleton
  public ChatMemory chatMemory() {

    return MessageWindowChatMemory.builder()
      .maxMessages(20)
      .chatMemoryStore(chatMemoryStore)
      .build();
  }


}
