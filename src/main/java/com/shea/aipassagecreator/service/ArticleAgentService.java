package com.shea.aipassagecreator.service;

import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shea.aipassagecreator.constant.PromptConstant;
import com.shea.aipassagecreator.domain.dto.ImageDTO;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import com.shea.aipassagecreator.enums.ArticleStyleEnum;
import com.shea.aipassagecreator.enums.ImageMethodEnum;
import com.shea.aipassagecreator.enums.SseMessageTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.shea.aipassagecreator.enums.SseMessageTypeEnum.*;

/**
 * 文章生成Agent
 * @author : Shea.
 * @since : 2026/5/20 19:38
 */
@Service
@Slf4j
public class ArticleAgentService {

    @Resource
    private ChatModel dashScopeChatModel;
//    @Resource
//    @Qualifier("nanoBananaService")
//    private IImageSearchService imageSearchService;
    @Resource
    private CosService cosService;
    @Resource
    private ImageServiceStrategy imageServiceStrategy;

    /**
     * 执行文章生成
     * @param state 文章状态
     * @param streamHandler 流处理
     */
    public void executeArticleGeneration(ArticleState state, Consumer<String> streamHandler) {
        // 智能体1：生成标题
        log.info("智能体1：开始生成标题，taskId={}", state.getTaskId());
        agent1GenerateTitle(state);
        streamHandler.accept(AGENT1_COMPLETE.getValue());
        // 智能体2：生成大纲
        log.info("智能体2：开始生成大纲，taskId={}", state.getTaskId());
        agent2GenerateOutline(state,streamHandler);
        streamHandler.accept(AGENT2_COMPLETE.getValue());
        // 智能体3：生成内容
        log.info("智能体3：开始生成内容，taskId={}", state.getTaskId());
        agent3GenerateContent(state,streamHandler);
        streamHandler.accept(AGENT3_COMPLETE.getValue());
        // 智能体4：分析配图需求
        log.info("智能体4：开始分析配图需求，taskId={}", state.getTaskId());
        agent4AnalyzeImageRequirement(state);
        streamHandler.accept(AGENT4_COMPLETE.getValue());
        // 智能体5：生成配图
        log.info("智能体5：开始生成配图，taskId={}", state.getTaskId());
        agent5GenerateImages(state,streamHandler);
        streamHandler.accept(AGENT5_COMPLETE.getValue());
        // 图文合成：将配图插入正文
        log.info("开始图文合成，taskId={}", state.getTaskId());
        mergeImagesIntoContent(state);
        streamHandler.accept(MERGE_COMPLETE.getValue());
    }

    /**
     * 图文合成：将配图插入正文
     * @param state 文章状态
     */
    private void mergeImagesIntoContent(ArticleState state) {
        String content = state.getContent();
        List<ArticleState.ImageResult> images = state.getImages();
        if (images == null || images.isEmpty()) {
            state.setFullContent(content);
            return;
        }
        String fullContent = content;
        for (ArticleState.ImageResult image : images) {
            String placeholder = image.getPlaceholderId();
            if (placeholder != null && !placeholder.isEmpty()) {
                String imageMarkdown = "![" + image.getDescription() + "]( " + image.getUrl() + ")";
                fullContent = fullContent.replace(placeholder, imageMarkdown);
            }
        }
//        StringBuilder sb = new StringBuilder();
//        String[] split = content.split("\n");
//        for (String s : split) {
//            sb.append(s).append("\n");
//            if (s.startsWith("## ")) {
//                String sectionTitle = s.substring(3).trim();
//                insertImageAfterSection(sb,images,sectionTitle);
//            }
//        }
//        state.setFullContent(sb.toString());
//        log.info("图文合成完成，fullContentLength={}", sb.length());
        state.setFullContent(fullContent);
        log.info("图文合成完成，fullContentLength={}", fullContent.length());
    }

    /**
     * 在指定section标题后插入图片
     * @param sb StringBuilder
     * @param images 图片列表
     * @param sectionTitle section标题
     */
    private void insertImageAfterSection(StringBuilder sb, List<ArticleState.ImageResult> images, String sectionTitle) {
        for (ArticleState.ImageResult image : images) {
            if (image.getPosition() > 1 && sectionTitle.contains(image.getSectionTitle().trim())) {
                sb.append("\n![")
                        .append(image.getDescription()).append("]( ")
                        .append(image.getUrl()).append(")\n");
                break;
            }
        }
    }

    /**
     * 智能体5：生成配图
     * @param state 文章状态
     * @param streamHandler 流处理
     */
    private void agent5GenerateImages(ArticleState state, Consumer<String> streamHandler) {
        List<ArticleState.ImageResult> imageResults = new ArrayList<>();
        for (ArticleState.ImageRequirement requirement : state.getImageRequirementList()) {
            log.info("智能体5：开始生成配图，position={},keywords={}", requirement.getPosition(), requirement.getKeywords());
//            String imageUrl = imageSearchService.searchImage(requirement.getKeywords());
            ImageDTO dto = ImageDTO.builder()
                    .keywords(requirement.getKeywords())
                    .prompt(requirement.getPrompt())
                    .position(requirement.getPosition())
                    .type(requirement.getType())
                    .build();
            ImageServiceStrategy.ImageResult result = imageServiceStrategy.getImageAndUpload(requirement.getImageSource(), dto);
            String cosUrl = result.cosUrl();
            ImageMethodEnum method = result.method();
            ArticleState.ImageResult imageResult = buildImageResult(requirement, cosUrl, method);
            // 降级策略
//            ImageMethodEnum method = imageSearchService.getMethod();
//            if (imageUrl == null) {
//                imageUrl = imageSearchService.getFallbackImage(requirement.getPosition());
//                method = ImageMethodEnum.PICSUM;
//                log.warn("智能体5：图片检索失败，使用降级方案，position={}", requirement.getPosition());
//            }
//            String finalImageUrl = cosService.useDirectUrl(imageUrl);
            // 构建配图结果
//            ArticleState.ImageResult imageResult = buildImageResult(requirement,finalImageUrl,method);
            imageResults.add(imageResult);

            // 发送配图生成完成的消息
            String imageCompleteMessage = IMAGE_COMPLETE.getStreamingPrefix() + JSONUtil.toJsonStr(imageResult);
            streamHandler.accept(imageCompleteMessage);
            log.info("智能体5：单张配图生成完成，position={},method={}", requirement.getPosition(), method.getValue());
        }
        state.setImages(imageResults);
        log.info("智能体5：所有配图生成完成，count={}", imageResults.size());
    }

    /**
     * 构建配图结果
     * @param requirement 配图需求
     * @param finalImageUrl 最终图片URL
     * @param method 配图方法
     * @return 配图结果
     */
    private ArticleState.ImageResult buildImageResult(ArticleState.ImageRequirement requirement, String finalImageUrl, ImageMethodEnum method) {
        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
        imageResult.setPosition(requirement.getPosition());
        imageResult.setSectionTitle(requirement.getSectionTitle());
        imageResult.setKeywords(requirement.getKeywords());
        imageResult.setDescription(requirement.getType());
        imageResult.setUrl(finalImageUrl);
        imageResult.setMethod(method.getValue());
        imageResult.setPlaceholderId(requirement.getPlaceholderId());
        return imageResult;
    }

    /**
     * 智能体4：分析配图需求
     * @param state 文章状态
     */
    private void agent4AnalyzeImageRequirement(ArticleState state) {
//        String prompt = PromptConstant.AGENT4_IMAGE_REQUIREMENTS_PROMPT
//                .replace("{mainTitle}",state.getTitle().getMainTitle())
//                .replace("{content}",state.getContent());
//        String content = callLlm(prompt);
//        List<ArticleState.ImageRequirement> imageRequirements = parseJsonListResponse(content,ArticleState.ImageRequirement.class,"配图需求");
//        state.setImageRequirementList(imageRequirements);
//        log.info("智能体4：配图需求分析完成，imageRequirementList={}", imageRequirements.size());
        String availableMethods = buildAvailableMethodsDescription(state.getEnabledImageMethods());
        String prompt = PromptConstant.AGENT4_IMAGE_REQUIREMENTS_PROMPT
                .replace("{mainTitle}",state.getTitle().getMainTitle())
                .replace("{content}",state.getContent())
                .replace("{availableMethods}", availableMethods);
        String content = callLlm(prompt);
        ArticleState.Agent4Result agent4Result = parseJsonResponse(content, ArticleState.Agent4Result.class, "配图需求");
        state.setContent(agent4Result.getContentWithPlaceholders());
        state.setImageRequirementList(agent4Result.getImageRequirements());
        log.info("智能体4：配图需求分析完成，count={}，已在正文中插入占位符", agent4Result.getImageRequirements().size());
    }

    /**
     * 构建可用的配图方法描述
     * @param enabledImageMethods 启用的配图方法
     * @return 描述
     */
    private String buildAvailableMethodsDescription(List<String> enabledImageMethods) {
        if (enabledImageMethods == null || enabledImageMethods.isEmpty()) {
            return getAllMethodsDescription();
        }
        StringBuilder sb = new StringBuilder();
        for (String method : enabledImageMethods) {
            ImageMethodEnum methodEnum = ImageMethodEnum.getByValue(method);
            if (methodEnum != null && !methodEnum.isFallback()) {
                sb.append("    - ")
                        .append(methodEnum.getValue())
                        .append(": ")
                        .append(getMethodUsageDescription(methodEnum))
                        .append("\n");
            }
        }
        return sb.toString();

    }

    /**
     * 获取配图方式的使用说明
     * @param method 配图方式
     * @return 使用说明
     */
    private String getMethodUsageDescription(ImageMethodEnum method) {
        return switch (method) {
            case PEXELS -> "适合真实场景、产品照片、人物照片、自然风景等写实图片";
            case NANO_BANANA -> "适合创意插画、信息图表、需要文字渲染、抽象概念、艺术风格等 AI 生成图片";
            case MERMAID -> "适合流程图、架构图、时序图、关系图、甘特图等结构化图表";
            case ICONIFY -> "适合图标、符号、小型装饰性图标（如：箭头、勾选、星星、心形等）";
            case EMOJI_PACK -> "适合表情包、搞笑图片、轻松幽默的配图";
            case SVG_DIAGRAM -> "适合概念示意图、思维导图样式、逻辑关系展示（不涉及精确数据）";
            default -> method.getDescription();
        };
    }

    /**
     * 获取所有配图方式的完整描述
     * @return 描述
     */
    private String getAllMethodsDescription() {
        return """
               - PEXELS: 适合真实场景、产品照片、人物照片、自然风景等写实图片
               - NANO_BANANA: 适合创意插画、信息图表、需要文字渲染、抽象概念、艺术风格等 AI 生成图片
               - MERMAID: 适合流程图、架构图、时序图、关系图、甘特图等结构化图表
               - ICONIFY: 适合图标、符号、小型装饰性图标（如：箭头、勾选、星星、心形等）
               - EMOJI_PACK: 适合表情包、搞笑图片、轻松幽默的配图
               - SVG_DIAGRAM: 适合概念示意图、思维导图样式、逻辑关系展示（不涉及精确数据）
               """;
    }

    /**
     * 解析JSON列表响应
     * @param content 响应内容
     * @param clazz 类型
     * @param name 名称
     * @return 列表
     */
    private <T> List<T> parseJsonListResponse(String content, Class<T> clazz, String name) {
        try {
            return JSONUtil.toList(content, clazz);
        } catch (JSONException e) {
            log.info("{}, JSON解析失败,content={}", name, content);
            JSONObject obj = JSONUtil.parseObj(content);
            return obj.getJSONArray("imageRequirements").toList(clazz);
        } catch (Exception e) {
            log.error("{}, JSON解析失败,content={}", name, content, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 调用LLM
     * @param prompt 提示词
     * @return LLM的响应
     */
    private String callLlm(String prompt) {
        ChatResponse response = dashScopeChatModel.call(new Prompt(new UserMessage(prompt)));
        return response.getResult().getOutput().getText();
    }

    /**
     * 智能体3：生成内容
     * @param state 文章状态
     * @param streamHandler 流处理
     */
    private void agent3GenerateContent(ArticleState state, Consumer<String> streamHandler) {
        String outlineText = JSONUtil.toJsonStr(state.getOutline().getSections());
        String prompt = PromptConstant.AGENT3_CONTENT_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{subTitle}", state.getTitle().getSubTitle())
                .replace("{outline}", outlineText)
                + getStylePrompt(state.getStyle());
        String content = callLlmWithStreaming(prompt,streamHandler,AGENT3_STREAMING);
        state.setContent(content);
        log.info("智能体3：内容生成完成，contentLength={}", content.length());
    }

    /**
     * 调用LLM并返回流式响应
     * @param prompt 提示词
     * @param streamHandler 流处理
     * @param sseMessageTypeEnum 消息类型
     * @return LLM的响应
     */
    private String callLlmWithStreaming(String prompt, Consumer<String> streamHandler, SseMessageTypeEnum sseMessageTypeEnum) {
        StringBuilder sb = new StringBuilder();
        Flux<ChatResponse> streamResp = dashScopeChatModel.stream(new Prompt(new UserMessage(prompt)));
        streamResp
                .doOnNext(resp -> {
                    String chunk = resp.getResult().getOutput().getText();
                    if (chunk != null && !chunk.isEmpty()) {
                        sb.append(chunk);
                        streamHandler.accept(sseMessageTypeEnum.getStreamingPrefix() + chunk);
                    }
                })
                .doOnError(error -> log.error("LLM流式调用失败，messageType={}",sseMessageTypeEnum,error))
                .blockLast();
        return sb.toString();
    }

    /**
     * 智能体2：生成大纲
     * @param state 文章状态
     * @param streamHandler 流处理
     */
    private void agent2GenerateOutline(ArticleState state, Consumer<String> streamHandler) {
        String prompt = PromptConstant.AGENT2_OUTLINE_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{subTitle}", state.getTitle().getSubTitle())
                + getStylePrompt(state.getStyle());

        String content = callLlmWithStreaming(prompt,streamHandler,AGENT2_STREAMING);
        ArticleState.OutlineResult outlineResult = parseJsonResponse(content,ArticleState.OutlineResult.class,"大纲");
        state.setOutline(outlineResult);
        log.info("智能体2：大纲生成完成，sections={}", outlineResult.getSections().size());
    }

    /**
     * 解析JSON响应
     * @param content 响应内容
     * @param clazz 类型
     * @param name 名称
     * @return 对象
     */
    private <T> T parseJsonResponse(String content, Class<T> clazz, String name) {
        try {
            return JSONUtil.toBean(content,clazz);
        }catch (Exception e) {
            log.error("{}, JSON解析失败,content={}", name, content, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 智能体1：生成标题
     * @param state 文章状态
     */
    private void agent1GenerateTitle(ArticleState state) {
        String prompt = PromptConstant.AGENT1_TITLE_PROMPT
                .replace("{topic}",state.getTopic())
                + getStylePrompt(state.getStyle());

        String content = callLlm(prompt);
        ArticleState.TitleResult titleResult = parseJsonResponse(content,ArticleState.TitleResult.class,"标题");
        state.setTitle(titleResult);
        log.info("智能体1：标题生成完成，mainTitle={}", titleResult.getMainTitle());
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
