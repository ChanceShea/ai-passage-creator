package com.shea.aipassagecreator.service.impl;

import cn.hutool.core.util.StrUtil;
import com.shea.aipassagecreator.config.EmojiPackConfig;
import com.shea.aipassagecreator.enums.ImageMethodEnum;
import com.shea.aipassagecreator.service.IImageSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.shea.aipassagecreator.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * 表情包服务实现类
 * @author : Shea.
 * @since : 2026/5/24 19:12
 */
@Service
@Slf4j
public class EmojiPackService implements IImageSearchService {

    @Resource
    private EmojiPackConfig emojiPackConfig;

    @Override
    public String searchImage(String keywords) {
        if (StrUtil.isEmpty(keywords)) {
            log.warn("表情包搜索关键词为空,keywords={}", keywords);
            return null;
        }
        try {
            String searchText = keywords + emojiPackConfig.getSuffix();
            log.info("表情包搜索关键词：{} -> {}",keywords, searchText);
            // 构建搜索URL
            String fetchUrl = buildSearchUrl(searchText);
            // Jsoup获取页面
            Document document = Jsoup.connect(fetchUrl)
                    .timeout(emojiPackConfig.getTimeout())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
            // 定位图片容器
            Element div = document.getElementsByClass("dgControl").first();
            if (div == null) {
                log.warn("Bing 未找到图片容器，keywords={}", keywords);
                return null;
            }
            // CSS选择器提取图片元素
            Elements imgElements = div.select("img.mimg");
            if(imgElements.isEmpty()) {
                log.warn("Bing未检索到表情包，keywords={},searchText={}", keywords, searchText);
                return null;
            }
            // 获取图片URL
            String imageUrl = imgElements.get(0).attr("src");
            if (StrUtil.isEmpty(imageUrl)) {
                log.warn("Bing图片URL为空，keywords={}", keywords);
                return null;
            }
            // 清理URL参数
            imageUrl = cleanImageUrl(imageUrl);
            log.info("表情包检索成功: {} -> {}", keywords, imageUrl);
            return imageUrl;
        }catch (Exception e) {
            log.error("表情包检索异常，keywords={}", keywords, e);
            return null;
        }
    }

    /**
     * 清理图片URL中的参数
     * @param imageUrl 图片URL
     * @return 清理后的图片URL
     */
    private String cleanImageUrl(String imageUrl) {
        if (StrUtil.isEmpty(imageUrl)) {
            return null;
        }

        int questionMarkIndex = imageUrl.indexOf("?");
        if (questionMarkIndex > 0) {
            return imageUrl.substring(0, questionMarkIndex);
        }
        return imageUrl;
    }

    /**
     * 构建搜索URL
     * @param searchText 搜索关键词
     * @return 搜索URL
     */
    private String buildSearchUrl(String searchText) {
        String encodedText = URLEncoder.encode(searchText, StandardCharsets.UTF_8);
        return String.format("%s?q=%s&mmasync=1",
                emojiPackConfig.getSearchUrl(),
                encodedText
                );
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.EMOJI_PACK;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
