package com.shea.aipassagecreator.agent.parallel;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.aipassagecreator.agent.context.StreamHandlerContext;
import com.shea.aipassagecreator.agent.tools.ImageGenerationTool;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import com.shea.aipassagecreator.enums.SseMessageTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 并行图片生成器
 * @author : Shea.
 * @since : 2026/6/5 16:33
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ParallelImageGenerator implements NodeAction {

    private final ImageGenerationTool imageGenerationTool;
    public static final String INPUT_IMAGE_REQUIREMENTS = "inputImageRequirements";
    public static final String OUTPUT_IMAGES = "images";

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        @SuppressWarnings("unchecked")
        List<ArticleState.ImageRequirement> imageRequirements =
                (List<ArticleState.ImageRequirement>) state.value(INPUT_IMAGE_REQUIREMENTS)
                .map(v -> {
                    if (v instanceof List<?> list) {
                        if (list.isEmpty()) {
                            return new ArrayList<ArticleState.ImageRequirement>();
                        }
                        if (list.get(0) instanceof ArticleState.ImageRequirement imageRequirement) {
                            return imageRequirement;
                        }
                        return convert2ImageRequirements(list);
                    }
                    return new ArrayList<ArticleState.ImageRequirement>();
                }).orElse(new ArrayList<>());

        // 从ThreadLocal中获取流式处理器
        Consumer<String> consumer = StreamHandlerContext.get();
        log.info("ParallelImageGenerator开始执行：配图需求数量={}",imageRequirements.size());
        if (imageRequirements.isEmpty()) {
            log.info("没有配图需求，跳过图片生成");
            return Map.of(OUTPUT_IMAGES,new ArrayList<>());
        }
        // 按照ImageSource进行分组
        Map<String, List<ArticleState.ImageRequirement>> groupedBySource = imageRequirements.stream()
                .collect(Collectors.groupingBy(ArticleState.ImageRequirement::getImageSource));
        log.info("配图需求按类型分组:{}",
                groupedBySource.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, e -> e.getValue().size()
                        )));
        // 并行生成不同类型的图片
        List<ArticleState.ImageResult> allImages = executeParallel(groupedBySource, consumer);
        // 按照position进行排序
        allImages.sort((a,b) -> {
            Integer posA = a.getPosition() == null ? 0 : a.getPosition();
            Integer posB = b.getPosition() == null ? 0 : b.getPosition();
            return posA.compareTo(posB);
        });

        log.info("ParallelImageGenerator执行完成：成功生成{}张图片",allImages.size());
        return Map.of(OUTPUT_IMAGES,allImages);
    }

    /**
     * 并行执行图片生成
     * @param groupedBySource 按照ImageSource分组的图片需求
     * @param consumer 消费者
     * @return 生成的图片结果
     */
    private List<ArticleState.ImageResult> executeParallel(
            Map<String, List<ArticleState.ImageRequirement>> groupedBySource,
            Consumer<String> consumer
    ) {
        CopyOnWriteArrayList<ArticleState.ImageResult> allImages = new CopyOnWriteArrayList<>();

        List<CompletableFuture<Void>> futures = groupedBySource.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    String imageSource = entry.getKey();
                    List<ArticleState.ImageRequirement> requirements = entry.getValue();
                    log.info("开始处理{}类型的图片，数量：{}", imageSource, requirements.size());
                    for (ArticleState.ImageRequirement req : requirements) {
                        try {
                            ImageGenerationTool.ImageGenerationResult result = imageGenerationTool.generateImageDirect(
                                    req.getImageSource(),
                                    req.getKeywords(),
                                    req.getPrompt(),
                                    req.getPosition(),
                                    req.getType(),
                                    req.getSectionTitle(),
                                    req.getPlaceholderId()
                            );
                            if (result.isSuccess()) {
                                ArticleState.ImageResult imageResult = convert2ImageResult(result);
                                allImages.add(imageResult);
                                if (consumer != null) {
                                    String message =
                                            SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix()
                                                    + JSONUtil.toJsonStr(imageResult);
                                    consumer.accept(message);
                                }
                                log.info("图片生成成功：imageSource={},position={}", imageSource, req.getPosition());
                            } else {
                                log.warn("图片生成失败：imageSource={},position={}", imageSource, req.getPosition());
                            }
                        } catch (Exception e) {
                            log.error("图片生成异常：imageSource={},position={}", imageSource, req.getPosition(), e);
                        }
                    }
                    log.info("完成处理{}类型的图片", imageSource);
                })).toList();
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return allImages;
    }

    /**
     * 转换ImageGenerationResult为ArticleState.ImageResult
     * @param result 图片生成结果
     * @return 图片结果
     */
    private ArticleState.ImageResult convert2ImageResult(ImageGenerationTool.ImageGenerationResult result) {
        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
        imageResult.setPosition(result.getPosition());
        imageResult.setUrl(result.getUrl());
        imageResult.setMethod(result.getMethod());
        imageResult.setKeywords(result.getKeywords());
        imageResult.setSectionTitle(result.getSectionTitle());
        imageResult.setDescription(result.getDescription());
        imageResult.setPlaceholderId(result.getPlaceholderId());
        return imageResult;
    }

    /**
     * 将列表转换为ImageRequirement列表
     * @param list 图片需求列表
     * @return 转换后的图片需求列表
     */
    private List<ArticleState.ImageRequirement> convert2ImageRequirements(List<?> list) {
        List<ArticleState.ImageRequirement> requirements = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof ArticleState.ImageRequirement requirement) {
                requirements.add(requirement);
            } else if (item instanceof Map) {
                String json = JSONUtil.toJsonStr(item);
                ArticleState.ImageRequirement requirement = JSONUtil.toBean(json, ArticleState.ImageRequirement.class);
                requirements.add(requirement);
            }
        }
        return requirements;
    }
}
