package com.shea.aipassagecreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Pexels 配置类
 * @author : Shea.
 * @since : 2026/5/20 20:17
 */
@Configuration
@ConfigurationProperties(prefix = "pexels")
@Data
public class PexelsConfig {

    private String apiKey;
}
