package com.shea.aipassagecreator.constant;

/**
 * 文章常量
 * @author : Shea.
 * @since : 2026/5/20 20:21
 */
public interface ArticleConstant {

    /**
     * SSE 超时时间
     */
    long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    /**
     * SSE 重连时间
     */
    long SSE_RECONNECT_TIME_MS = 3000L;

    /**
     * Pexels API 的图片搜索 URL
     */
    String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    /**
     * Pexels API 的每页图片数量
     */
    int PEXELS_PER_PAGE = 1;

    /**
     * Pexels API 的图片方向：横向
     */
    String PEXELS_ORIENTATION_LANDSCAPE = "landscape";

    /**
     * Picsum 随机图片的 URL 模板
     */
    String PICSUM_URL_TEMPLATE = "https://picsum.photos/800/600?random=%d";

    /**
     * Bing 图片搜索 URL
     */
    String BING_IMAGE_SEARCH_URL = "https://cn.bing.com/images/async";

    /**
     * 表情包后缀
     */
    String EMOJI_PACK_SUFFIX = "表情包";

    /**
     * Bing 图片搜索结果最大数量
     */
    int BING_MAX_IMAGES = 30;


    // region SVG 绘图相关常量

    /**
     * SVG文件前缀
     */
    String SVG_FILE_PREFIX = "svg-chart";

    /**
     * SVG默认宽度
     */
    int SVG_DEFAULT_WIDTH = 800;

    /**
     * SVG默认高度
     */
    int SVG_DEFAULT_HEIGHT = 600;

    // endregion
}
