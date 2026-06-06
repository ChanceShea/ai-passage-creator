package com.shea.aipassagecreator.agent.agents;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.aipassagecreator.agent.tools.ImageGenerationTool;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图文合成Agent
 * @author : Shea.
 * @since : 2026/6/5 21:32
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ContentMergeAgent implements NodeAction {

    public static final String INPUT_CONTENT = "content";
    public static final String INPUT_IMAGES = "images";
    public static final String OUTPUT_FULL_CONTENT = "fullContent";

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        String content = state.value(INPUT_CONTENT)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("缺少正文参数"));

        @SuppressWarnings("unchecked")
        List<ArticleState.ImageResult> images = state.value(INPUT_IMAGES)
                .map(v -> {
                    if (v instanceof List<?> list) {
                        if (list.isEmpty()) {
                            return new ArrayList<ArticleState.ImageResult>();
                        }
                        if (list.getFirst() instanceof ArticleState.ImageResult) {
                            return (List<ArticleState.ImageResult>) v;
                        }
                        return convert2ImageResults(list);
                    }
                    return new ArrayList<ArticleState.ImageResult>();
                })
                .orElse(new ArrayList<>());
        log.info("ContentMergeAgent 开始执行：正文长度={}，图片数量={}",content.length(),images.size());

        String fullContent = mergeImagesIntoContent(content,images);

        log.info("ContentMergeAgent 执行完成：全文长度={}",fullContent.length());
        return Map.of(OUTPUT_FULL_CONTENT, fullContent);
    }

    /**
     * 将图片插入到内容中
     * @param content 正文
     * @param images 图片列表
     * @return 插入图片后的全文
     */
    private String mergeImagesIntoContent(String content, List<ArticleState.ImageResult> images) {
        if (images == null || images.isEmpty()) {
            return content;
        }
        String fullContent = content;
        for (ArticleState.ImageResult image : images) {
            String placeholder = image.getPlaceholderId();
            log.info("处理图片,position={},placeholderId={},url={}",
                    image.getPosition(),placeholder,image.getUrl());
            if (placeholder != null && !placeholder.isEmpty()) {
                String description = image.getDescription() != null ? image.getDescription() : "配图";
                String imageMarkdown = "![" + description + "](" + image.getUrl() + ")";
                if (fullContent.contains(placeholder)) {
                    fullContent = fullContent.replace(placeholder, imageMarkdown);
                    log.info("成功替换占位符：{} -> {}",
                            placeholder,imageMarkdown.substring(0,Math.min(50,imageMarkdown.length())));
                } else {
                    log.warn("正文中未找到占位符：{}",placeholder);
                }
            } else {
                log.warn("图片 position={}的placeholderId为空",image.getPosition());
            }
        }
        return fullContent;
    }

    /**
     * 将图片结果转换为ImageResult
     * @param list 图片结果列表
     * @return ImageResult列表
     */
    private List<ArticleState.ImageResult> convert2ImageResults(List<?> list) {
        List<ArticleState.ImageResult> results = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof ArticleState.ImageResult result) {
                results.add(result);
            } else if (item instanceof ImageGenerationTool.ImageGenerationResult genResult) {
                if (genResult.isSuccess()) {
                    ArticleState.ImageResult imageResult = getImageResult(genResult);
                    results.add(imageResult);
                }
            } else if (item instanceof Map) {
                String jsonStr = JSONUtil.toJsonStr(item);
                ArticleState.ImageResult imageResult = JSONUtil.toBean(jsonStr, ArticleState.ImageResult.class);
                if (imageResult.getUrl() != null) {
                    results.add(imageResult);
                }
            }
        }
        return results;
    }

    /**
     * 将ImageGenerationResult转换为ImageResult
     * @param genResult ImageGenerationResult
     * @return ImageResult
     */
    private ArticleState.ImageResult getImageResult(ImageGenerationTool.ImageGenerationResult genResult) {
        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
        imageResult.setUrl(genResult.getUrl());
        imageResult.setPosition(genResult.getPosition());
        imageResult.setMethod(genResult.getMethod());
        imageResult.setKeywords(genResult.getKeywords());
        imageResult.setSectionTitle(genResult.getSectionTitle());
        imageResult.setDescription(genResult.getDescription());
        imageResult.setPlaceholderId(genResult.getPlaceholderId());
        return imageResult;
    }
}
