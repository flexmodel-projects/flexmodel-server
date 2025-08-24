package tech.wetech.flexmodel.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
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
import tech.wetech.flexmodel.interfaces.rest.response.ChatResponseDelta;
import tech.wetech.flexmodel.util.JsonUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Path("/chat")
@ApplicationScoped
public class ChatResource {

  @Inject
  Sse sse;

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

  /**
   * 处理流式响应
   */
  private Response handleStreamResponse(ChatRequest request, SseEventSink eventSink) {
    if (eventSink.isClosed()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    CompletableFuture.runAsync(() -> {
      try {
        String userMessage = request.messages().getLast().content();
        String aiResponse = generateMockResponse(userMessage);
        String requestId = UUID.randomUUID().toString();
        String model = request.model() != null ? request.model() : "gpt-3.5-turbo";

        // 获取流式配置
        String streamStrategy = "character";
        int streamDelay = 50;
        int minChunkSize = 2;
        int maxChunkSize = 8;

        // 将文本拆分成更小的块进行流式返回
        List<String> textChunks = splitTextIntoChunks(aiResponse, streamStrategy, minChunkSize, maxChunkSize);

        // 发送初始的role消息
        sendInitialRoleEvent(eventSink, requestId, model);

        // 发送内容增量更新
        for (int i = 0; i < textChunks.size(); i++) {
          if (eventSink.isClosed()) {
            break;
          }

          String deltaContent = textChunks.get(i);
          boolean isLastChunk = (i == textChunks.size() - 1);

          sendDeltaEvent(i, eventSink, requestId, model, deltaContent, isLastChunk);

          // 使用配置的延迟时间
          TimeUnit.MILLISECONDS.sleep(streamDelay);
        }

        // 发送完成事件
        sendDoneEvent(eventSink);

      } catch (Exception e) {
        log.error("流式聊天出错", e);
        sendErrorEvent(eventSink, e.getMessage());
      } finally {
        eventSink.close();
      }
    });

    return Response.ok().build();
  }

  /**
   * 将文本拆分成小块
   * 支持多种拆分策略：按字符、按短语、按句子等
   */
  private List<String> splitTextIntoChunks(String text, String splitStrategy, int minChunkSize, int maxChunkSize) {
    List<String> chunks = new java.util.ArrayList<>();

    switch (splitStrategy) {
      case "character":
        // 按字符拆分，随机块大小
        int chunkSize = minChunkSize + (int) (Math.random() * (maxChunkSize - minChunkSize + 1));
        for (int i = 0; i < text.length(); i += chunkSize) {
          int endIndex = Math.min(i + chunkSize, text.length());
          String chunk = text.substring(i, endIndex);
          chunks.add(chunk);
        }
        break;

      case "phrase":
        // 按短语拆分（标点符号分割）
        String[] phrases = text.split("[，。！？；：、]");
        for (String phrase : phrases) {
          if (!phrase.trim().isEmpty()) {
            // 如果短语太长，再按字符拆分
            if (phrase.length() > maxChunkSize) {
              List<String> subChunks = splitTextIntoChunks(phrase, "character", minChunkSize, maxChunkSize);
              chunks.addAll(subChunks);
            } else {
              chunks.add(phrase.trim());
            }
          }
        }
        break;

      case "sentence":
        // 按句子拆分
        String[] sentences = text.split("[。！？]");
        for (String sentence : sentences) {
          if (!sentence.trim().isEmpty()) {
            // 如果句子太长，再按字符拆分
            if (sentence.length() > maxChunkSize * 2) {
              List<String> subChunks = splitTextIntoChunks(sentence, "character", minChunkSize, maxChunkSize);
              chunks.addAll(subChunks);
            } else {
              chunks.add(sentence.trim());
            }
          }
        }
        break;

      case "word":
        // 按单词拆分（空格分割）
        String[] words = text.split("\\s+");
        for (String word : words) {
          if (!word.trim().isEmpty()) {
            chunks.add(word.trim());
          }
        }
        break;

      default:
        // 默认按字符拆分
        for (char c : text.toCharArray()) {
          chunks.add(String.valueOf(c));
        }
        break;
    }

    // 确保至少有一个块
    if (chunks.isEmpty()) {
      chunks.add(text);
    }

    return chunks;
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

  private void sendDeltaEvent(int index, SseEventSink eventSink, String requestId, String model, String deltaContent, boolean isLastChunk) {
    try {
      ChatDelta delta = new ChatDelta(null, deltaContent);
      ChatChoiceDelta choice = new ChatChoiceDelta(index, delta, isLastChunk ? "stop" : null);
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

  private String generateMockResponse(String userMessage) {
    // 模拟AI响应逻辑
    if (userMessage.contains("你好") || userMessage.contains("hello")) {
      return "你好！我是AI助手，很高兴为您服务。有什么我可以帮助您的吗？";
    } else if (userMessage.contains("天气")) {
      return "抱歉，我无法获取实时天气信息。建议您查看天气预报应用或网站获取准确的天气信息。";
    } else if (userMessage.contains("时间")) {
      return "当前时间是：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } else if (userMessage.contains("帮助") || userMessage.contains("help")) {
      return "我可以帮助您回答各种问题，包括但不限于：\n1. 一般性问题咨询\n2. 技术问题解答\n3. 写作和创意建议\n4. 学习和教育支持\n\n请告诉我您需要什么帮助！";
    } else {
      return "我理解您的问题：" + userMessage + "。这是一个很有趣的话题，让我为您详细解答...";
    }
  }
}
