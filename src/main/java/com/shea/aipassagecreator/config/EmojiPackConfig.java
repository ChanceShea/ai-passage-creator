package com.shea.aipassagecreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.shea.aipassagecreator.constant.ArticleConstant.BING_IMAGE_SEARCH_URL;
import static com.shea.aipassagecreator.constant.ArticleConstant.EMOJI_PACK_SUFFIX;

/**
 * 表情包配置类
 * @author : Shea.
 * @since : 2026/5/24 19:10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "emoji-pack")
public class EmojiPackConfig {

    /**
     * 搜索图片的URL
     */
    private String searchUrl = BING_IMAGE_SEARCH_URL;

    /**
     * 表情包后缀
     */
    private String suffix = EMOJI_PACK_SUFFIX;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 10000;
}
