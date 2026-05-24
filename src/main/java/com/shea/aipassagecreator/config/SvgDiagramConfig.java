package com.shea.aipassagecreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.shea.aipassagecreator.constant.ArticleConstant.SVG_DEFAULT_HEIGHT;
import static com.shea.aipassagecreator.constant.ArticleConstant.SVG_DEFAULT_WIDTH;

/**
 * SVG概念示意图配置类
 * @author : Shea.
 * @since : 2026/5/24 19:22
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "svg-diagram")
public class SvgDiagramConfig {

    /**
     * 默认宽度
     */
    private Integer defaultWidth = SVG_DEFAULT_WIDTH;

    /**
     * 默认高度
     */
    private Integer defaultHeight = SVG_DEFAULT_HEIGHT;

    /**
     * COS存储文件夹
     */
    private String folder = "svg-diagrams";
}
