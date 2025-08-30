package tech.wetech.flexmodel.interfaces.rest;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.interfaces.rest.request.ChatRequest;
import tech.wetech.flexmodel.interfaces.rest.response.ChatChoiceDelta;
import tech.wetech.flexmodel.interfaces.rest.response.ChatDelta;
import tech.wetech.flexmodel.interfaces.rest.response.ChatMessage;
import tech.wetech.flexmodel.interfaces.rest.response.ChatResponseDelta;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Path("/chat")
@ApplicationScoped
public class ChatResource {

  @Inject
  Sse sse;

  @Inject
  StreamingChatModel model;

  // In-memory conversation store
  private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

  public ChatResource() {
    // 创建默认对话 - 技术咨询场景
    Conversation defaultConversation = new Conversation();
    defaultConversation.id = "default";
    defaultConversation.title = "Java 微服务架构讨论";
    defaultConversation.createdAt = LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    defaultConversation.messages.add(new ChatMessage("user", "你好，我想了解一下如何设计一个健壮的微服务架构？"));
    defaultConversation.messages.add(new ChatMessage("assistant", "你好！设计一个健壮的微服务架构需要考虑多个方面。首先，你需要确定服务边界，确保每个服务都有明确的职责。"));
    defaultConversation.messages.add(new ChatMessage("user", "那在服务间通信方面有什么推荐的做法吗？"));
    defaultConversation.messages.add(new ChatMessage("assistant", "对于服务间通信，通常有两种方式：同步通信（如 REST API、gRPC）和异步通信（如消息队列）。选择哪种方式取决于你的业务场景和性能要求。"));
    conversations.put("default", defaultConversation);

    // 创建技术对话 - 数据库优化
    Conversation dbConversation = new Conversation();
    dbConversation.id = "conv-001";
    dbConversation.title = "数据库性能优化";
    dbConversation.createdAt = LocalDateTime.now().minusDays(1).minusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    dbConversation.messages.add(new ChatMessage("user", "我的 MySQL 查询很慢，有什么优化建议吗？"));
    dbConversation.messages.add(new ChatMessage("assistant", "数据库查询慢可能有多种原因。你可以先检查是否缺少合适的索引，使用 EXPLAIN 分析查询执行计划，避免 SELECT * 等。"));
    dbConversation.messages.add(new ChatMessage("user", "我已经添加了索引，但还是不够快。"));
    dbConversation.messages.add(new ChatMessage("assistant", "那可以考虑分库分表、读写分离、查询缓存等方案。另外，检查表结构设计是否合理也很重要。"));
    conversations.put(dbConversation.id, dbConversation);

    // 创建日常对话
    Conversation casualConversation = new Conversation();
    casualConversation.id = "conv-002";
    casualConversation.title = "周末计划讨论";
    casualConversation.createdAt = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    casualConversation.messages.add(new ChatMessage("user", "周末有什么推荐的活动吗？"));
    casualConversation.messages.add(new ChatMessage("assistant", "这取决于你的兴趣！如果你喜欢户外活动，可以考虑徒步或骑行。如果喜欢室内活动，看电影或逛博物馆也不错。"));
    casualConversation.messages.add(new ChatMessage("user", "我想尝试一些新的餐厅，有什么推荐吗？"));
    casualConversation.messages.add(new ChatMessage("assistant", "你比较喜欢哪种菜系呢？中餐、西餐还是日韩料理？另外，你更倾向于高档餐厅还是特色小吃？"));
    casualConversation.messages.add(new ChatMessage("user", "我喜欢日韩料理，特别是性价比高的地方。"));
    casualConversation.messages.add(new ChatMessage("assistant", "那可以试试附近的日式拉面店或者韩式烤肉店。我推荐几家口碑不错的，你可以在大众点评上查看具体位置和评价。"));
    conversations.put(casualConversation.id, casualConversation);

    // 创建编程学习对话
    Conversation learningConversation = new Conversation();
    learningConversation.id = "conv-003";
    learningConversation.title = "学习 Java Spring Boot";
    learningConversation.createdAt = LocalDateTime.now().minusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    learningConversation.messages.add(new ChatMessage("user", "我想学习 Spring Boot，应该从哪里开始？"));
    learningConversation.messages.add(new ChatMessage("assistant", "学习 Spring Boot 建议先掌握 Java 基础和 Spring 框架的基本概念。然后可以从官方文档入手，跟着教程创建简单的项目。"));
    learningConversation.messages.add(new ChatMessage("user", "有没有推荐的学习资源？"));
    learningConversation.messages.add(new ChatMessage("assistant", "官方文档是最好的起点：https://spring.io/projects/spring-boot。此外，Baeldung 网站也有很多优质教程，还有 GitHub 上的 spring-boot-examples 项目。"));
    learningConversation.messages.add(new ChatMessage("user", "学习过程中遇到问题怎么办？"));
    learningConversation.messages.add(new ChatMessage("assistant", "遇到问题可以查阅官方文档、Stack Overflow 或者技术论坛。也可以加入一些技术交流群，与其他开发者交流经验。"));
    conversations.put(learningConversation.id, learningConversation);

    // 创建产品讨论对话
    Conversation productConversation = new Conversation();
    productConversation.id = "conv-004";
    productConversation.title = "新产品功能讨论";
    productConversation.createdAt = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    productConversation.messages.add(new ChatMessage("user", "我们计划为产品添加用户反馈功能，有什么建议吗？"));
    productConversation.messages.add(new ChatMessage("assistant", "用户反馈功能很重要。建议包括：1) 多种反馈渠道（应用内、邮件、社交媒体）；2) 反馈分类和标签；3) 反馈状态跟踪；4) 用户回复机制。"));
    productConversation.messages.add(new ChatMessage("user", "如何激励用户提供反馈？"));
    productConversation.messages.add(new ChatMessage("assistant", "可以通过奖励机制激励用户，比如积分、优惠券或者早期访问新功能的权限。同时确保反馈流程简单快捷，减少用户操作成本。"));
    conversations.put(productConversation.id, productConversation);
}


  public static class Conversation {
    public String id;
    public String title;
    public String createdAt;
    public List<ChatMessage> messages = new ArrayList<>();
  }

  public static class CreateConversationRequest {
    public String title;
  }

  public static class SendMessageRequest {
    public String content;
    public String model;
  }

  /**
   * 统一的聊天接口
   * 根据请求中的stream参数决定返回普通响应还是流式响应
   */
  @POST
  @Path("/completions")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response chatCompletions(ChatRequest request, @Context SseEventSink eventSink) {
    log.info("收到聊天请求: " + request.messages().get(0).content());
    // 流式响应
    return handleStreamResponse(request, eventSink);
  }

  // Conversations CRUD
  @POST
  @Path("/conversations")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createConversation(CreateConversationRequest request) {
    String id = UUID.randomUUID().toString();
    Conversation conversation = new Conversation();
    conversation.id = id;
    conversation.title = request != null && request.title != null && !request.title.isBlank() ? request.title : "New Chat";
    conversation.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    conversations.put(id, conversation);
    return Response.status(Response.Status.CREATED).entity(conversation).build();
  }

  @GET
  @Path("/conversations")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listConversations() {
    return Response.ok(conversations.values()).build();
  }

  @GET
  @Path("/conversations/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConversation(@PathParam("id") String id) {
    Conversation conversation = conversations.get(id);
    if (conversation == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(conversation).build();
  }

  @DELETE
  @Path("/conversations/{id}")
  public Response deleteConversation(@PathParam("id") String id) {
    Conversation removed = conversations.remove(id);
    if (removed == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.noContent().build();
  }

  // Conversation messages
  @GET
  @Path("/conversations/{id}/messages")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listMessages(@PathParam("id") String id) {
    Conversation conversation = conversations.get(id);
    if (conversation == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    List<ChatMessage> messages = conversation.messages == null ? Collections.emptyList() : conversation.messages;
    return Response.ok(messages).build();
  }

  @POST
  @Path("/conversations/{id}/messages")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response sendMessage(@PathParam("id") String id, SendMessageRequest request, @Context SseEventSink eventSink) {
    Conversation conversation = conversations.get(id);
    if (conversation == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (request == null || request.content == null || request.content.isBlank()) {
      return Response.status(Response.Status.BAD_REQUEST).entity("content is required").build();
    }

    // Append user message to conversation
    ChatMessage userMsg = new ChatMessage("user", request.content);
    conversation.messages.add(userMsg);

    // Build full message history for model
    List<dev.langchain4j.data.message.ChatMessage> history = new ArrayList<>();
    for (ChatMessage m : conversation.messages) {
      if ("assistant".equals(m.role())) {
        history.add(new AiMessage(m.content()));
      } else {
        history.add(new UserMessage(m.content()));
      }
    }

    String requestId = UUID.randomUUID().toString();
    String modelName = request.model != null ? request.model : "default";
    StringBuilder assistantContent = new StringBuilder();

    model.chat(history, new StreamingChatResponseHandler() {
      @Override
      public void onPartialResponse(String token) {
        assistantContent.append(token);
        sendDeltaEvent(0, eventSink, requestId, modelName, token);
      }

      @Override
      public void onCompleteResponse(ChatResponse chatResponse) {
        log.info("完成响应: {}", chatResponse.id());
        // Append assistant message to conversation on completion
        conversation.messages.add(new ChatMessage("assistant", assistantContent.toString()));
        sendDoneEvent(eventSink);
      }

      @Override
      public void onError(Throwable throwable) {
        log.error("流式聊天出错", throwable);
        sendErrorEvent(eventSink, throwable.getMessage());
      }
    });
    return Response.ok().build();
  }

  /**
   * 处理流式响应
   */
  private Response handleStreamResponse(ChatRequest request, SseEventSink eventSink) {
    if (eventSink.isClosed()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
    for (ChatMessage message : request.messages()) {

      if (message.role().equals("assistant")) {
        messages.add(new AiMessage(message.content()));
      } else {
        messages.add(new UserMessage(message.content()));
      }
    }
    String requestId = UUID.randomUUID().toString();

    model.chat(messages, new StreamingChatResponseHandler() {
      @Override
      public void onPartialResponse(String token) {
        sendDeltaEvent(0, eventSink, requestId, request.model(), token);
      }

      @Override
      public void onCompleteResponse(ChatResponse chatResponse) {
        log.info("完成响应: {}", chatResponse.id());
        sendDoneEvent(eventSink);
      }

      @Override
      public void onError(Throwable throwable) {
        log.error("流式聊天出错", throwable);
        sendErrorEvent(eventSink, throwable.getMessage());
      }
    });
    return Response.ok().build();
  }


  private void sendDoneEvent(SseEventSink eventSink) {
    try (eventSink) {
      // 直接发送[DONE]，不要添加额外的data:前缀
      OutboundSseEvent event = sse.newEventBuilder()
        .data("[DONE]")
        .build();
      eventSink.send(event);
    } catch (Exception e) {
      log.error("发送完成事件失败", e);
    }
  }

  private void sendErrorEvent(SseEventSink eventSink, String errorMessage) {
    try {
      String errorData = "{\"error\": {\"message\": \"" + errorMessage + "\"}}";
      OutboundSseEvent event = sse.newEventBuilder()
        .data(errorData)
        .build();
      eventSink.send(event);
    } catch (Exception e) {
      log.error("发送错误事件失败", e);
    }
  }

  private void sendInitialRoleEvent(SseEventSink eventSink, String requestId, String model) {
    try {
      ChatDelta delta = new ChatDelta("assistant", "");
      ChatChoiceDelta choice = new ChatChoiceDelta(0, delta, null);
      ChatResponseDelta response = new ChatResponseDelta(
        "chat.completion.chunk",
        requestId,
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        model,
        List.of(choice),
        null
      );

      String jsonData = JsonUtils.getInstance().stringify(response);
      OutboundSseEvent event = sse.newEventBuilder()
        .data(jsonData)
        .build();
      eventSink.send(event);
    } catch (Exception e) {
      log.error("发送初始角色事件失败", e);
    }
  }

  private void sendDeltaEvent(int index, SseEventSink eventSink, String requestId, String model, String deltaContent) {
    try {
      ChatDelta delta = new ChatDelta(null, deltaContent);
      ChatChoiceDelta choice = new ChatChoiceDelta(index, delta, null);
      ChatResponseDelta response = new ChatResponseDelta(
        "chat.completion.chunk",
        requestId,
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        model,
        List.of(choice),
        null
      );

      String jsonData = JsonUtils.getInstance().stringify(response);
      OutboundSseEvent event = sse.newEventBuilder()
        .data(jsonData)
        .build();
      eventSink.send(event);
    } catch (Exception e) {
      log.error("发送增量事件失败", e);
    }
  }

}
