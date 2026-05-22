package com.shea.aipassagecreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * COS配置类
 * @author : Shea.
 * @since : 2026/5/20 21:18
 */
@Configuration
@ConfigurationProperties(prefix = "cos")
@Data
public class CosConfig {

    private String secretId;
    private String secretKey;
    private String bucket;
    private String region;
}
