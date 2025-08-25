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
import tech.wetech.flexmodel.util.JsonUtils;

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
