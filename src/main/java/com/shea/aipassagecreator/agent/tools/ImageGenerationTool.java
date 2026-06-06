package com.shea.aipassagecreator.agent.tools;

import cn.hutool.json.JSONUtil;
import com.shea.aipassagecreator.domain.dto.ImageDTO;
import com.shea.aipassagecreator.enums.ImageMethodEnum;
import com.shea.aipassagecreator.service.CosService;
import com.shea.aipassagecreator.service.ImageServiceStrategy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 图片生成工具
 * @author : Shea.
 * @since : 2026/6/5 10:51
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ImageGenerationTool {

    private final ImageServiceStrategy strategy;
    private final CosService cosService;

    /**
     * 根据需求生成或搜索图片。支持多种图片来源
     */
    @Tool(description = "根据需求生成或搜索图片。支持多种图片来源")
    public String generateImage(
            @ToolParam(description = "图片来源类型") String imageSource,
            @ToolParam(description = "搜索关键词") String keywords,
            @ToolParam(description = "AI生图提示词或图表代码") String prompt,
            @ToolParam(description = "图片位置序号") Integer position,
            @ToolParam(description = "图片类型：cover或section") String type,
            @ToolParam(description = "对应的章节标题") String sectionTitle
    ) {
        log.info("ImageGenerationTool 开始执行：imageSource={}，position={}",imageSource,position);
        try {
            Result generateImage = getGenerateImage(imageSource, keywords, prompt, position, type, sectionTitle);
            log.info("ImageGenerationTool执行成功：position={},method={}",position, generateImage.method().getValue());
            return JSONUtil.toJsonStr(generateImage.imageGenerationResult());
        }catch (Exception e) {
            log.error("ImageGenerationTool执行失败：position={},method={}",position,e.getMessage());
            ImageGenerationResult imageGenerationResult = new ImageGenerationResult();
            imageGenerationResult.setPosition(position);
            imageGenerationResult.setSuccess(false);
            imageGenerationResult.setError(e.getMessage());
            imageGenerationResult.setSectionTitle(sectionTitle);
            return JSONUtil.toJsonStr(imageGenerationResult);
        }
    }

    @NotNull
    private Result getGenerateImage(String imageSource, String keywords, String prompt, Integer position, String type, String sectionTitle) {
        ImageDTO imageDTO = ImageDTO.builder()
                .keywords(keywords)
                .prompt(prompt)
                .position(position)
                .type(type)
                .build();
        ImageServiceStrategy.ImageResult result = strategy.getImageAndUpload(imageSource, imageDTO);
        String cosUrl = result.cosUrl();
        ImageMethodEnum method = result.method();

        ImageGenerationResult imageGenerationResult = new ImageGenerationResult();
        imageGenerationResult.setPosition(position);
        imageGenerationResult.setUrl(cosUrl);
        imageGenerationResult.setMethod(method.getValue());
        imageGenerationResult.setKeywords(keywords);
        imageGenerationResult.setSectionTitle(sectionTitle);
        imageGenerationResult.setDescription(type);
        imageGenerationResult.setSuccess(true);
        Result generateImage = new Result(method, imageGenerationResult);
        return generateImage;
    }

    private record Result(ImageMethodEnum method, ImageGenerationResult imageGenerationResult) {
    }

    /**
     * 直接生成图片
     */
    public ImageGenerationResult generateImageDirect(
            String imageSource,
            String keywords,
            String prompt,
            Integer position,
            String type,
            String sectionTitle,
            String placeholderId
    ) {
        try {
            Result generateImage = getGenerateImage(imageSource, keywords, prompt, position, type, sectionTitle);
            generateImage.imageGenerationResult().setPlaceholderId(placeholderId);
            return generateImage.imageGenerationResult();
        }catch (Exception e) {
            log.error("ImageGenerationTool执行失败：position={},method={}",position,e.getMessage());
            ImageGenerationResult imageGenerationResult = new ImageGenerationResult();
            imageGenerationResult.setPosition(position);
            imageGenerationResult.setSuccess(false);
            imageGenerationResult.setError(e.getMessage());
            imageGenerationResult.setSectionTitle(sectionTitle);
            imageGenerationResult.setPlaceholderId(placeholderId);
            return imageGenerationResult;
        }
    }
    /**
     * 图片生成结果
     */
    @Data
    public static class ImageGenerationResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;
        private String placeholderId;
        private boolean success;
        private String error;
    }
}
