package com.shea.aipassagecreator.service.impl;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.shea.aipassagecreator.config.PexelsConfig;
import com.shea.aipassagecreator.enums.ImageMethodEnum;
import com.shea.aipassagecreator.service.IImageSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import static com.shea.aipassagecreator.constant.ArticleConstant.*;

/**
 * Pexels 搜索图片服务
 * @author : Shea.
 * @since : 2026/5/20 20:15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PexelsService implements IImageSearchService {

    private final PexelsConfig pexelsConfig;
    private final OkHttpClient client = new OkHttpClient();


    @Override
    public String searchImage(String keywords) {
        try{
            String url = buildSearchUrl(keywords);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", pexelsConfig.getApiKey())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Pexels API 调用失败: {}", response.code());
                    return null;
                }
                String responseBody = response.body().string();
                return extractImageUrl(responseBody, keywords);
            }
        }catch (Exception e) {
            log.error("Pexels API 调用异常", e);
            return null;
        }
    }

    /**
     * 从响应中提取URL
     * @param responseBody 响应体
     * @param keywords 关键字
     * @return 图片URL
     */
    private String extractImageUrl(String responseBody, String keywords) {
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray photos = jsonObject.getAsJsonArray("photos");
        if (photos.isEmpty()) {
            log.warn("Pexels未检测到图片: {}", keywords);
            return null;
        }

        JsonObject photo = photos.get(0).getAsJsonObject();
        JsonObject src = photo.getAsJsonObject("src");
        return src.get("large").getAsString();
    }

    private String buildSearchUrl(String keywords) {
        return String.format("%s?query=%s&per_page=%d&orientation=%s",
                PEXELS_API_URL,
                keywords,
                PEXELS_PER_PAGE, PEXELS_ORIENTATION_LANDSCAPE);
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.PEXELS;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE,position);
    }
}
