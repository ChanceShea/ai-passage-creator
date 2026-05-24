package com.shea.aipassagecreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Iconify配置类
 * @author : Shea.
 * @since : 2026/5/24 18:50
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "iconify")
public class IconifyConfig {

    /**
     * Iconify API地址
     */
    private String apiUrl = "https://api.iconify.design";

    /**
     * Iconify搜索结果限制
     */
    private Integer searchLimit = 10;

    /**
     * Iconify默认宽度
     */
    private Integer defaultHeight = 64;

    /**
     * Iconify默认颜色
     */
    private String defaultColor = "";
}
