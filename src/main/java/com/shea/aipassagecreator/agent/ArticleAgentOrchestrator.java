package com.shea.aipassagecreator.agent;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.shea.aipassagecreator.agent.agents.*;
import com.shea.aipassagecreator.agent.config.AgentConfig;
import com.shea.aipassagecreator.agent.context.StreamHandlerContext;
import com.shea.aipassagecreator.agent.parallel.ParallelImageGenerator;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import com.shea.aipassagecreator.enums.SseMessageTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 文章只能以编排器，通过StateGraph编排多个Agent
 *
 * @author : Shea.
 * @since : 2026/6/5 22:09
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleAgentOrchestrator {

    private final AgentConfig agentConfig;
    private final TitleGeneratorAgent titleGeneratorAgent;
    private final OutlineGeneratorAgent outlineGeneratorAgent;
    private final ContentGeneratorAgent contentGeneratorAgent;
    private final ImageAnalyzerAgent imageAnalyzerAgent;
    private final ParallelImageGenerator parallelImageGenerator;
    private final ContentMergeAgent contentMergeAgent;

    // region 状态键常量

    private static final String KEY_TASK_ID = "taskId";
    private static final String KEY_TOPIC = "topic";
    private static final String KEY_STYLE = "style";
    private static final String KEY_USER_DESCRIPTION = "userDescription";
    private static final String KEY_MAIN_TITLE = "mainTitle";
    private static final String KEY_SUB_TITLE = "subTitle";
    private static final String KEY_TITLE_OPTIONS = "titleOptions";
    private static final String KEY_OUTLINE = "outline";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_CONTENT_WITH_PLACEHOLDERS = "contentWithPlaceholders";
    private static final String KEY_IMAGES = "images";
    private static final String KEY_IMAGES_REQUIREMENTS = "imageRequirements";
    private static final String KEY_FULL_CONTENT = "fullContent";
    private static final String KEY_ENABLED_IMAGE_METHODS = "enabledImageMethods";

    // endregion

    public void executePhase1_GenerateTitles(ArticleState state, Consumer<String> streamHandler) {
        log.info("阶段1（多智能体安排）：开始生成标题方案，taskId={}", state.getTaskId());
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put(KEY_TASK_ID, state.getTaskId());
            inputs.put(KEY_TOPIC, state.getTopic());
            inputs.put(KEY_STYLE, state.getStyle());

            StateGraph graph = buildPhase1Graph();
            CompiledGraph compiled = graph.compile();
            Optional<OverAllState> result = compiled.invoke(inputs);
            if (result.isPresent()) {
                OverAllState finalState = result.get();
                @SuppressWarnings("unchecked")
                List<ArticleState.TitleOption> titleOptions =
                        (List<ArticleState.TitleOption>) finalState
                                .value(KEY_TITLE_OPTIONS)
                                .orElse(null);
                if (titleOptions != null) {
                    state.setTitleOptions(titleOptions);
                    streamHandler.accept(SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
                    log.info("阶段1（多智能体编排）：标题方案生成完成，taskId={}", state.getTaskId());
                } else {
                    throw new RuntimeException("标题方案生成失败：结果为空");
                }
            } else {
                throw new RuntimeException("标题方案生成失败：结果为空");
            }
        } catch (Exception e) {
            log.error("阶段1（多智能体编排）：标题方案生成失败，taskId={}", state.getTaskId(), e);
            throw new RuntimeException("标题方案生成失败，" + e.getMessage(), e);
        }
    }

    /**
     * 执行阶段2：生成大纲
     *
     * @param state     状态
     * @param streamHandler 流处理
     */
    public void executePhase2_GenerateOutline(ArticleState state, Consumer<String> streamHandler) {
        log.info("阶段2（多智能体编排）：开始生成大纲，taskId={}", state.getTaskId());
        // 设置流式处理器到ThreadLocal中
        StreamHandlerContext.set(streamHandler);
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put(KEY_TASK_ID, state.getTaskId());
            inputs.put(KEY_MAIN_TITLE, state.getTitle().getMainTitle());
            inputs.put(KEY_SUB_TITLE, state.getTitle().getSubTitle());
            inputs.put(KEY_USER_DESCRIPTION, state.getUserDescription());
            inputs.put(KEY_STYLE, state.getStyle());

            StateGraph graph = buildPhase2Graph();
            CompiledGraph compiled = graph.compile();
            Optional<OverAllState> result = compiled.invoke(inputs);
            if (result.isPresent()) {
                OverAllState finalState = result.get();
                ArticleState.OutlineResult outline =
                        (ArticleState.OutlineResult) finalState
                                .value(KEY_OUTLINE)
                                .orElse(null);
                if (outline != null) {
                    state.setOutline(outline);
                    streamHandler.accept(SseMessageTypeEnum.AGENT2_COMPLETE.getValue());
                    log.info("阶段2（多智能体编排）：大纲生成完成，章节数={}", outline.getSections().size());
                } else {
                    throw new RuntimeException("大纲生成失败：结果为空");
                }
            } else {
                throw new RuntimeException("大纲生成失败：结果为空");
            }
        } catch (Exception e) {
            log.error("阶段2（多智能体编排）：大纲生成失败，taskId={}", state.getTaskId(), e);
            throw new RuntimeException("大纲生成失败，" + e.getMessage(), e);
        } finally {
            // 清理ThreadLocal中的流式处理器
            StreamHandlerContext.clear();
        }
    }

    public void executePhase3_GenerateContent(ArticleState state, Consumer<String> streamHandler) {
        log.info("阶段3（多智能体编排）：开始生成正文+配图，taskId={}", state.getTaskId());
        StreamHandlerContext.set(streamHandler);
        try {
            Map<String,Object> inputs = new HashMap<>();
            inputs.put(KEY_TASK_ID, state.getTaskId());
            inputs.put(KEY_MAIN_TITLE, state.getTitle().getMainTitle());
            inputs.put(KEY_SUB_TITLE, state.getTitle().getSubTitle());
            inputs.put(KEY_OUTLINE, state.getOutline());
            inputs.put(KEY_STYLE, state.getStyle());
            inputs.put(KEY_ENABLED_IMAGE_METHODS, state.getEnabledImageMethods());
            StateGraph graph = buildPhase3Graph();
            CompiledGraph compiled = graph.compile();
            Optional<OverAllState> result = compiled.invoke(inputs);
            if (result.isPresent()) {
                OverAllState finalState = result.get();
                String contentWithPlaceholders = finalState.value(KEY_CONTENT_WITH_PLACEHOLDERS)
                        .map(Object::toString)
                        .orElse(null);
                String content = finalState.value(KEY_CONTENT)
                        .map(Object::toString)
                        .orElse(null);

                @SuppressWarnings("unchecked")
                List<ArticleState.ImageRequirement> imageRequirements =
                        (List<ArticleState.ImageRequirement>) finalState
                                .value(KEY_IMAGES_REQUIREMENTS).orElse(null);

                @SuppressWarnings("unchecked")
                List<ArticleState.ImageResult> images =
                        (List<ArticleState.ImageResult>) finalState
                                .value(KEY_IMAGES).orElse(null);

                String fullContent = finalState.value(KEY_FULL_CONTENT)
                        .map(Object::toString)
                        .orElse(null);
                if (contentWithPlaceholders != null) {
                    state.setContent(contentWithPlaceholders);
                } else if (content != null) {
                    state.setContent(content);
                }
                streamHandler.accept(SseMessageTypeEnum.AGENT3_COMPLETE.getValue());
                if (imageRequirements != null) {
                    state.setImageRequirementList(imageRequirements);
                    streamHandler.accept(SseMessageTypeEnum.AGENT4_COMPLETE.getValue());
                }
                if (images != null) {
                    state.setImages(images);
                    streamHandler.accept(SseMessageTypeEnum.AGENT5_COMPLETE.getValue());
                }
                if (fullContent != null) {
                    state.setFullContent(fullContent);
                    streamHandler.accept(SseMessageTypeEnum.MERGE_COMPLETE.getValue());
                }
                log.info("阶段3（多智能体编排）：正文+配图生成完成，正文长度={}，图片数={}",
                        contentWithPlaceholders != null ? contentWithPlaceholders.length() : 0,
                        images != null ? images.size() : 0);
            } else {
                throw new RuntimeException("正文+配图生成失败：执行结果为空");
            }
        } catch (Exception e) {
            log.error("阶段3（多智能体编排）：正文+配图生成失败，taskId={}", state.getTaskId(), e);
            throw new RuntimeException("正文+配图生成失败，" + e.getMessage(), e);
        } finally {
            StreamHandlerContext.clear();
        }
    }

    /**
     * 构建阶段3的StateGraph
     *
     * @return StateGraph
     * @throws GraphStateException 状态异常
     */
    private StateGraph buildPhase3Graph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = createKeyStrategyFactory();
        return new StateGraph(keyStrategyFactory)
                .addNode("content_generator", node_async(contentGeneratorAgent))
                .addNode("image_analyzer", node_async(imageAnalyzerAgent))
                .addNode("parallel_image_generator", node_async(parallelImageGenerator))
                .addNode("content_merge", node_async(contentMergeAgent))
                .addEdge(StateGraph.START, "content_generator")
                .addEdge("content_generator", "image_analyzer")
                .addEdge("image_analyzer","parallel_image_generator")
                .addEdge("parallel_image_generator", "content_merge")
                .addEdge("content_merge", StateGraph.END);
    }

    /**
     * 构建阶段2的StateGraph
     *
     * @return StateGraph
     * @throws GraphStateException 状态异常
     */
    private StateGraph buildPhase2Graph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = createKeyStrategyFactory();
        return new StateGraph(keyStrategyFactory)
                .addNode("outline_generator", node_async(outlineGeneratorAgent))
                .addEdge(StateGraph.START, "outline_generator")
                .addEdge("outline_generator", StateGraph.END);

    }

    /**
     * 构建阶段1的StateGraph
     *
     * @return StateGraph
     * @throws GraphStateException 状态异常
     */
    private StateGraph buildPhase1Graph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = createKeyStrategyFactory();
        return new StateGraph(keyStrategyFactory)
                .addNode("title_generator", node_async(titleGeneratorAgent))
                .addEdge(StateGraph.START, "title_generator")
                .addEdge("title_generator", StateGraph.END);
    }

    /**
     * 创建KeyStrategyFactory，所有状态键都使用替换策略
     *
     * @return 状态键策略工厂
     */
    private KeyStrategyFactory createKeyStrategyFactory() {
        return () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put(KEY_TASK_ID, new ReplaceStrategy());
            strategies.put(KEY_TOPIC, new ReplaceStrategy());
            strategies.put(KEY_STYLE, new ReplaceStrategy());
            strategies.put(KEY_USER_DESCRIPTION, new ReplaceStrategy());
            strategies.put(KEY_MAIN_TITLE, new ReplaceStrategy());
            strategies.put(KEY_SUB_TITLE, new ReplaceStrategy());
            strategies.put(KEY_TITLE_OPTIONS, new ReplaceStrategy());
            strategies.put(KEY_OUTLINE, new ReplaceStrategy());
            strategies.put(KEY_CONTENT, new ReplaceStrategy());
            strategies.put(KEY_CONTENT_WITH_PLACEHOLDERS, new ReplaceStrategy());
            strategies.put(KEY_IMAGES, new ReplaceStrategy());
            strategies.put(KEY_FULL_CONTENT, new ReplaceStrategy());
            strategies.put(KEY_ENABLED_IMAGE_METHODS, new ReplaceStrategy());
            return strategies;
        };
    }
}
