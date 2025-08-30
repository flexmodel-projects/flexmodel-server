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
import tech.wetech.flexmodel.application.AiApplicationService;
import tech.wetech.flexmodel.codegen.entity.AiChatConversation;
import tech.wetech.flexmodel.codegen.entity.AiChatMessage;
import tech.wetech.flexmodel.interfaces.rest.request.ChatRequest;
import tech.wetech.flexmodel.interfaces.rest.response.ChatChoiceDelta;
import tech.wetech.flexmodel.interfaces.rest.response.ChatDelta;
import tech.wetech.flexmodel.interfaces.rest.response.ChatMessage;
import tech.wetech.flexmodel.interfaces.rest.response.ChatResponseDelta;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
@Path("/f/chat")
@ApplicationScoped
public class AiChatResource {

  @Inject
  Sse sse;

  @Inject
  StreamingChatModel model;

  @Inject
  AiApplicationService aiApplicationService;

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

  @POST
  @Path("/conversations")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public AiChatConversation createConversation(CreateConversationRequest request) {
    String title = request != null && request.title != null && !request.title.isBlank() ? request.title : "New Chat";
    return aiApplicationService.createConversation(title);
  }

  @GET
  @Path("/conversations")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AiChatConversation> listConversations() {
    return aiApplicationService.getAllConversations();
  }

  @GET
  @Path("/conversations/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public AiChatConversation getConversation(@PathParam("id") String id) {
    return aiApplicationService.getConversationById(id);
  }

  @DELETE
  @Path("/conversations/{id}")
  public void deleteConversation(@PathParam("id") String id) {
    aiApplicationService.deleteConversation(id);
  }

  // Conversation messages
  @GET
  @Path("/conversations/{id}/messages")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AiChatMessage> listMessages(@PathParam("id") String id) {
    return aiApplicationService.getMessagesByConversationId(id);
  }

  @POST
  @Path("/conversations/{id}/messages")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response sendMessage(@PathParam("id") String id, SendMessageRequest request, @Context SseEventSink eventSink) {
    AiChatConversation conversation = aiApplicationService.getConversationById(id);
    if (conversation == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (request == null || request.content == null || request.content.isBlank()) {
      return Response.status(Response.Status.BAD_REQUEST).entity("content is required").build();
    }

    // Build full message history for model
    List<dev.langchain4j.data.message.ChatMessage> history = new ArrayList<>();
    for (AiChatMessage m : conversation.getMessages()) {
      if ("assistant".equals(m.getRole())) {
        history.add(new AiMessage(m.getContent()));
      } else {
        history.add(new UserMessage(m.getContent()));
      }
    }

    String requestId = UUID.randomUUID().toString();
    String modelName = request.model != null ? request.model : "default";
    StringBuilder assistantContent = new StringBuilder();

    model.chat(history, new StreamingChatResponseHandler() {
      @Override
      public void onPartialResponse(String token) {
        assistantContent.append(token);
        sendDeltaEvent(0, eventSink, null, requestId, modelName, token);
      }

      @Override
      public void onCompleteResponse(ChatResponse chatResponse) {
        log.info("完成响应: {}", chatResponse.id());
        // Append assistant message to conversation on completion
        AiChatMessage aiChatMessage = new AiChatMessage();
        aiChatMessage.setRole("assistant");
        aiChatMessage.setContent(assistantContent.toString());
        aiApplicationService.saveMessage(aiChatMessage);
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
    ChatMessage lastMessage = request.messages().getLast();
    String conversationId;
    if (request.conversationId() == null) {
      AiChatConversation conversation = aiApplicationService.createConversation(lastMessage.content());
      conversationId = conversation.getId();
    } else {
      conversationId = request.conversationId();
    }
    AiChatMessage userMsg = new AiChatMessage();
    userMsg.setConversationId(conversationId);
    userMsg.setRole(lastMessage.role());
    userMsg.setContent(lastMessage.content());
    aiApplicationService.saveMessage(userMsg);
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
        sendDeltaEvent(0, eventSink, conversationId, requestId, request.model(), token);
      }

      @Override
      public void onCompleteResponse(ChatResponse chatResponse) {
        log.info("完成响应: {}", chatResponse.id());
        sendDoneEvent(eventSink);
        // Append assistant message to conversation on completion
        AiChatMessage aiMsg = new AiChatMessage();
        aiMsg.setId(requestId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(chatResponse.aiMessage().text());
        aiMsg.setConversationId(conversationId);
        aiApplicationService.saveMessage(aiMsg);
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
    try {
      // 直接发送[DONE]，不要添加额外的data:前缀
      OutboundSseEvent event = sse.newEventBuilder()
        .data("[DONE]")
        .build();
      eventSink.send(event);
    } catch (Exception e) {
      log.error("发送完成事件失败", e);
    } finally {
      if (!eventSink.isClosed()) {
        eventSink.close();
      }
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


  private void sendDeltaEvent(int index, SseEventSink eventSink, String conversationId, String requestId, String model, String deltaContent) {
    try {
      ChatDelta delta = new ChatDelta(null, deltaContent);
      ChatChoiceDelta choice = new ChatChoiceDelta(index, delta, null);
      ChatResponseDelta response = new ChatResponseDelta(
        "chat.completion.chunk",
        conversationId,
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
