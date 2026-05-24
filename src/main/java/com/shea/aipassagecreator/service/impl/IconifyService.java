package com.shea.aipassagecreator.service.impl;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.shea.aipassagecreator.config.IconifyConfig;
import com.shea.aipassagecreator.enums.ImageMethodEnum;
import com.shea.aipassagecreator.service.IImageSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.shea.aipassagecreator.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * Iconify图片搜索服务实现类
 * @author : Shea.
 * @since : 2026/5/24 18:52
 */
@Service
@Slf4j
public class IconifyService implements IImageSearchService {

    @Resource
    private IconifyConfig iconifyConfig;
    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    public String searchImage(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            log.warn("Iconify 搜索关键词为空");
            return null;
        }
        try {
            String searchUrl = buildSearchUrl(keywords);
            String searchRequest = callApi(searchUrl);

            if (searchRequest == null) {
                return null;
            }

            String iconName = extractFirstIcon(searchRequest);
            if (iconName == null) {
                log.warn("Iconify 未检索到图标：{}", keywords);
                return null;
            }

            String svgUrl = buildSvgUrl(iconName);
            log.info("Iconify 图标检索成功：{} -> {}", keywords, iconName);
            return svgUrl;
        } catch (Exception e) {
            log.error("Iconify 图标检索异常，keywords={}", keywords, e);
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.ICONIFY;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    /**
     * 构建搜索URL，用于从Iconify API获取图标数据
     *
     * @param keywords 搜索关键词
     * @return 搜索URL
     */
    private String buildSearchUrl(String keywords) {
        String encodeKeywords = URLEncoder.encode(keywords, StandardCharsets.UTF_8);
        return String.format("%s/search?query=%s&limit=%d",
                iconifyConfig.getApiUrl(),
                encodeKeywords,
                iconifyConfig.getSearchLimit()
        );
    }

    /**
     * 调用Iconify API，获取图标数据
     *
     * @param searchUrl 搜索URL
     * @return 图标数据
     */
    private String callApi(String searchUrl) {
        try {
            Request request = new Request.Builder()
                    .url(searchUrl)
                    .build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Iconify API调用失败: {}", response.code());
                    return null;
                }
                return response.body().string();
            }
        } catch (IOException e) {
            log.error("Iconify API调用异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Iconify API响应中提取第一个图标名称
     *
     * @param jsonResponse Iconify API响应的JSON字符串
     * @return 图标名称
     */
    private String extractFirstIcon(String jsonResponse) {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray icons = json.getAsJsonArray("icons");
            if (icons == null || icons.isEmpty()) {
                return null;
            }
            return icons.get(0).getAsString();
        } catch (Exception e) {
            log.error("解析Iconify搜索结果失败", e);
            return null;
        }
    }

    /**
     * 构建SVG图标URL
     *
     * @param iconName 图标名称(prefix:name)
     * @return SVG图标URL
     */
    private String buildSvgUrl(String iconName) {
        // 将mdi:home转换为mdi/home
        String path = iconName.replace(":","/");

        StringBuilder url = new StringBuilder(iconifyConfig.getApiUrl())
                .append("/")
                .append(path)
                .append(".svg");

        boolean hasParam = false;
        // 添加高度参数
        if (iconifyConfig.getDefaultHeight() != null && iconifyConfig.getDefaultHeight() > 0) {
            url.append("?height=").append(iconifyConfig.getDefaultHeight());
            hasParam = true;
        }
        // 添加颜色参数
        if (iconifyConfig.getDefaultColor() != null && !iconifyConfig.getDefaultColor().isEmpty()) {
            url.append(hasParam ? "&" : "?");
            // 处理颜色格式（#000000转换为%23000000）
            String color = iconifyConfig.getDefaultColor();
            if (color.startsWith("#")) {
                color = "%23" + color.substring(1);
            }
            url.append("color=").append(color);
        }
        return url.toString();
    }
}