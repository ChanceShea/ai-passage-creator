package com.shea.aipassagecreator.agent.agents;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.aipassagecreator.agent.context.StreamHandlerContext;
import com.shea.aipassagecreator.constant.PromptConstant;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import com.shea.aipassagecreator.enums.ArticleStyleEnum;
import com.shea.aipassagecreator.enums.SseMessageTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 正文生成Agent
 * @author : Shea.
 * @since : 2026/6/4 20:46
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ContentGeneratorAgent implements NodeAction {

    private final DashScopeChatModel dashScopeChatModel;

    public static final String INPUT_MAIN_TITLE = "mainTitle";
    public static final String INPUT_SUB_TITLE = "subTitle";
    public static final String INPUT_OUTLINE = "outline";
    public static final String INPUT_STYLE = "style";
    public static final String OUTPUT_CONTENT = "content";


    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String mainTitle = state.value(INPUT_MAIN_TITLE)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("缺少主标题参数"));
        String subTitle = state.value(INPUT_SUB_TITLE)
                .map(Object::toString)
                .orElse("");
        ArticleState.OutlineResult outline = state.value(INPUT_OUTLINE)
                .map(v -> {
                    if (v instanceof ArticleState.OutlineResult outlineResult) {
                        return outlineResult;
                    }
                    return JSONUtil.toBean(JSONUtil.toJsonStr(v), ArticleState.OutlineResult.class);
                })
                .orElseThrow(() -> new IllegalArgumentException("缺少大纲参数"));

        String style = state.value(INPUT_STYLE)
                .map(Object::toString)
                .orElse(null);
        log.info("ContentGeneratorAgent开始执行：mainTitle={}", mainTitle);

        String outlineText = JSONUtil.toJsonStr(outline.getSections());
        String prompt = PromptConstant.AGENT3_CONTENT_PROMPT
                .replace("{mainTitle}", mainTitle)
                .replace("{subTitle}", subTitle)
                .replace("{outline}", outlineText)
                + getStylePrompt(style);

        Consumer<String> consumer = StreamHandlerContext.get();
        String content = callLlmWithStreaming(prompt,consumer);
        log.info("ContentGeneratorAgent执行完毕：正文长度={}", content.length());
        return Map.of(OUTPUT_CONTENT, content);
    }

    /**
     * 调用LLM并返回结果
     * @param prompt 提示词
     * @param consumer 消费者
     * @return 结果
     */
    private String callLlmWithStreaming(String prompt, Consumer<String> consumer) {
        StringBuilder sb = new StringBuilder();
        Flux<ChatResponse> flux = dashScopeChatModel.stream(new Prompt(new UserMessage(prompt)));
        flux.doOnNext(response -> {
            String chunk = response.getResult().getOutput().getText();
            if (chunk != null && !chunk.trim().isEmpty()) {
                sb.append(chunk);
                if (consumer != null) {
                    consumer.accept(SseMessageTypeEnum.AGENT3_STREAMING.getValue() + chunk);
                }
            }
        }).doOnError(e -> log.error("LLM流式调用出错：{}", e.getMessage())).blockLast();
        return sb.toString();
    }

    /**
     * 根据风格获取对应的Prompt附加内容
     * @param style 样式
     * @return 提示词
     */
    private String getStylePrompt(String style) {
        if (style == null || style.isEmpty()) {
            return "";
        }

        ArticleStyleEnum styleEnum = ArticleStyleEnum.getEnumByValue(style);
        if (styleEnum == null) {
            return "";
        }

        return switch (styleEnum) {
            case TECH -> PromptConstant.STYLE_TECH_PROMPT;
            case EMOTIONAL -> PromptConstant.STYLE_EMOTIONAL_PROMPT;
            case EDUCATIONAL -> PromptConstant.STYLE_EDUCATIONAL_PROMPT;
            case HUMOROUS -> PromptConstant.STYLE_HUMOROUS_PROMPT;
        };
    }
}
