package dev.flexmodel.domain.model.ai.llm;

import dev.langchain4j.service.*;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

/**
 * @author cjbi
 */
@RegisterAiService
@SystemMessage("你是Flexmodel的AI助手，请利用检索到的内容回答用户问题")
public interface FlexmodelChatService {

  @UserMessage("""
    请回答：{message}.
    """)
  @ToolBox({ModelingTool.class})
  TokenStream chat(@MemoryId String sessionId, @V("message") String message);

}
