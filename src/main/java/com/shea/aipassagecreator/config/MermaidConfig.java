package com.shea.aipassagecreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Mermaid配置类
 * @author : Shea.
 * @since : 2026/5/24 16:08
 */
@Configuration
@ConfigurationProperties(prefix = "mermaid")
@Data
public class MermaidConfig {

    /**
     * CLI 命令，用于生成图片
     */
    private String cliCommand = "mmdc.cmd";

    /**
     * 背景颜色
     */
    private String backgroundColor = "transparent";

    /**
     * 输出格式
     */
    private String outputFormat = "svg";

    /**
     * 图片宽度（像素）
     */
    private Integer width = 1200;

    /**
     * 命令执行超时时间（毫秒）
     */
    private Long timeout = 30000L;
}
