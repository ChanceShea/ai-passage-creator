package com.shea.aipassagecreator.service.impl;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.shea.aipassagecreator.config.NanoBananaConfig;
import com.shea.aipassagecreator.domain.dto.ImageDTO;
import com.shea.aipassagecreator.domain.dto.ImageData;
import com.shea.aipassagecreator.enums.ImageMethodEnum;
import com.shea.aipassagecreator.service.IImageSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import static com.shea.aipassagecreator.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * Nano Banana服务
 * @author : Shea.
 * @since : 2026/5/24 15:12
 */
//@Service
@Slf4j
public class NanoBananaService implements IImageSearchService {

    @Resource
    private NanoBananaConfig nanoBananaConfig;

    @Override
    public String searchImage(String keywords) {
        return null;
    }

    @Override
    public ImageData getImageData(ImageDTO dto) {
        String prompt = dto.getEffectiveParam(true);
        return generateImageData(prompt);
    }

    public ImageData generateImageData(String prompt) {
        try {
            try (Client genaiClient = Client.builder()
                    .apiKey(nanoBananaConfig.getApiKey())
                    .build()) {
                // 构建图片配置
                ImageConfig.Builder builder = ImageConfig.builder()
                        .aspectRatio(nanoBananaConfig.getAspectRatio());
                String model = nanoBananaConfig.getModel();
                if (model != null && model.contains("gemini-3-pro")) {
                    builder.imageSize(nanoBananaConfig.getImageSize());
                }
                GenerateContentConfig config = GenerateContentConfig.builder()
                        .responseModalities("TEXT", "IMAGE")
                        .imageConfig(builder.build())
                        .build();
                log.info("Nano Banana开始生成图片，model={}, prompt={}", model, prompt);
                GenerateContentResponse response = genaiClient.models.generateContent(model != null ? model : "gemini-2.5-flash-image", prompt, config);
                if (response.parts() != null) {
                    for (Part part : response.parts()) {
                        if (part.inlineData().isPresent()) {
                            Blob blob = part.inlineData().get();
                            if (blob.data().isPresent()) {
                                byte[] imageBytes = blob.data().get();
                                String mimeType = blob.mimeType().orElse("image/png");
                                log.info("Nano Banana图片生成成功，size={} bytes,mimeType={}", imageBytes.length, mimeType);
                                return ImageData.fromBytes(imageBytes, mimeType);
                            }
                        }
                    }
                }
                log.warn("Nano Banana图片生成失败，prompt={}", prompt);
                return null;
            }
        }catch (Exception e) {
            log.error("Nano Banana生成图片异常，prompt={}", prompt);
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.NANO_BANANA;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE,position);
    }
}
