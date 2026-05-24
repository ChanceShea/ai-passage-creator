package com.shea.aipassagecreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * NanoBanana（Gemini AI生图）配置类
 * @author : Shea.
 * @since : 2026/5/24 14:33
 */
@Configuration
@ConfigurationProperties(prefix = "nano-banana")
@Data
public class NanoBananaConfig {

    /**
     * Gemini API密钥
     */
    private String apiKey;

    private String model = "gemini-2.5-flash-image";

    private String aspectRatio = "16:9";

    private String imageSize = "1K";

    private String outputMimeType = "image/png";
}
