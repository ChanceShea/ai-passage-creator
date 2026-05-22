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
}
