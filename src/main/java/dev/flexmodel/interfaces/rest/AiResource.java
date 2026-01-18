package dev.flexmodel.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.application.AiApplicationService;
import dev.flexmodel.codegen.entity.AiChatConversation;
import dev.flexmodel.codegen.entity.AiChatMessage;
import dev.flexmodel.domain.model.ai.llm.FlexmodelChatService;
import dev.flexmodel.interfaces.rest.response.ChatChoiceDelta;
import dev.flexmodel.interfaces.rest.response.ChatDelta;
import dev.flexmodel.interfaces.rest.response.ChatResponseDelta;
import dev.flexmodel.shared.SessionContextHolder;
import dev.flexmodel.shared.utils.JsonUtils;
import dev.flexmodel.shared.utils.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


@Slf4j
@Path("/v1/ai")
@ApplicationScoped
public class AiResource {

  @Inject
  Sse sse;

  @Inject
  AiApplicationService aiApplicationService;

  @Inject
  FlexmodelChatService flexmodelChatService;

  @POST
  @Path("/chat/conversations")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public AiChatConversation createConversation(CreateConversationRequest request) {
    String title = request != null && request.title != null && !request.title.isBlank() ? request.title : "New Chat";
    return aiApplicationService.createConversation(title);
  }

  @GET
  @Path("/chat/conversations")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AiChatConversation> getAllConversations() {
    return aiApplicationService.getAllConversations();
  }

  @GET
  @Path("/chat/conversations/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public AiChatConversation getConversation(@PathParam("id") String id) {
    return aiApplicationService.getConversationById(id);
  }

  @DELETE
  @Path("/chat/conversations/{id}")
  public void deleteConversation(@PathParam("id") String id) {
    aiApplicationService.deleteConversation(id);
  }

  @POST
  @Path("/chat/completions")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response sendMessage(SendMessageRequest request, @Context SseEventSink eventSink) {
    if (request.content == null || request.content.isBlank()) {
      return Response.status(Response.Status.BAD_REQUEST).entity("content is required").build();
    }
    String projectId = SessionContextHolder.getProjectId();
    // 会话不存在则创建会话
    String conversationId = StringUtils.isBlank(request.conversationId) ?
      aiApplicationService.createConversation(request.content).getId() : request.conversationId;
    AiChatMessage userMsg = new AiChatMessage();
    userMsg.setRole("user");
    userMsg.setContent(request.content);
    userMsg.setConversationId(conversationId);
    userMsg.setProjectId(projectId);
    aiApplicationService.saveMessage(userMsg);
    String requestId = UUID.randomUUID().toString();
    try {
      flexmodelChatService.chat(conversationId, request.content)
        .onPartialResponse(token -> {
          sendDeltaEvent(0, eventSink, conversationId, requestId, "", token);
        })
        .onCompleteResponse(chatResponse -> {
          log.info("完成响应: {}", chatResponse.id());
          // Append assistant message to conversation on completion
          if (chatResponse.aiMessage().text() != null) {
            AiChatMessage aiMsg = new AiChatMessage();
            aiMsg.setId(requestId);
            aiMsg.setRole("assistant");
            aiMsg.setContent(chatResponse.aiMessage().text());
            aiMsg.setConversationId(conversationId);
            aiMsg.setProjectId(projectId);
            aiApplicationService.saveMessage(aiMsg);
          }
          sendDoneEvent(eventSink);
        })
        .onError(throwable -> {
          log.error("流式聊天出错", throwable);
          sendErrorEvent(eventSink, throwable.getMessage());
        }).start();
    } catch (Exception e) {
      log.error("流式聊天出错", e);
      sendErrorEvent(eventSink, e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }

    return Response.accepted().build();
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
    } finally {
      if (!eventSink.isClosed()) {
        eventSink.close();
      }
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

  public static class CreateConversationRequest {
    public String projectId;
    public String title;
  }

  public static class SendMessageRequest {
    public String conversationId;
    @NotBlank
    public String content;
  }

}
