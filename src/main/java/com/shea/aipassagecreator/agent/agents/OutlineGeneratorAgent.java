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
 * 大纲生成Agent
 * @author : Shea.
 * @since : 2026/6/4 20:26
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OutlineGeneratorAgent implements NodeAction {

    private final DashScopeChatModel dashScopeChatModel;

    public static final String INPUT_MAIN_TITLE = "mainTitle";
    public static final String INPUT_SUB_TITLE = "subTitle";
    public static final String INPUT_USER_DESCRIPTION = "userDescription";
    public static final String INPUT_STYLE = "style";
    public static final String OUTPUT_OUTLINE = "outline";

    @Override
    public Map<String,Object> apply(OverAllState state) throws Exception {
        String mainTitle = state.value(INPUT_MAIN_TITLE)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("缺少主标题参数"));

        String subTitle = state.value(INPUT_SUB_TITLE)
                .map(Object::toString)
                .orElse("");

        String userDescription = state.value(INPUT_USER_DESCRIPTION)
                .map(Object::toString)
                .orElse(null);

        String style = state.value(INPUT_STYLE)
                .map(Object::toString)
                .orElse(null);

        log.info("OutlineGeneratorAgent 开始执行：mainTitle={},subTitle={}", mainTitle, subTitle);

        String descriptionSection = "";
        if (userDescription != null && !userDescription.trim().isEmpty()) {
            descriptionSection = PromptConstant.AGENT2_DESCRIPTION_SECTION
                    .replace("{userDescription}", userDescription);
        }
        String prompt = PromptConstant.AGENT2_OUTLINE_PROMPT
                .replace("{mainTitle}", mainTitle)
                .replace("{subTitle}", subTitle)
                .replace("{descriptionSection}", descriptionSection)
                 + getStylePrompt(style);

        Consumer<String> consumer = StreamHandlerContext.get();
        String content = callLlmWithStreaming(prompt, consumer);
        ArticleState.OutlineResult outlineResult = JSONUtil.toBean(content, ArticleState.OutlineResult.class);
        log.info("OutlineGeneratorAgent执行完成：生成了{}个章节",outlineResult.getSections().size());
        return Map.of(OUTPUT_OUTLINE, outlineResult);
    }

    /**
     * 调用LLM并处理流式响应
     * @param prompt 提示词
     * @param consumer 消费者
     * @return 响应内容
     */
    private String callLlmWithStreaming(String prompt, Consumer<String> consumer) {
        StringBuilder sb = new StringBuilder();
        Flux<ChatResponse> streamResponse = dashScopeChatModel.stream(new Prompt(new UserMessage(prompt)));
        streamResponse.doOnNext(response -> {
            String chunk = response.getResult().getOutput().getText();
            if (chunk != null && !chunk.trim().isEmpty()) {
                sb.append(chunk);
                if (consumer != null) {
                    consumer.accept(SseMessageTypeEnum.AGENT2_STREAMING.getStreamingPrefix() + chunk);
                }
            }
        }).doOnError(err -> log.error("OutlineGeneratorAgent流式调用失败,",err))
                .blockLast();
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
