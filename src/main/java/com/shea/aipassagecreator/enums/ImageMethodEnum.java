package com.shea.aipassagecreator.enums;

import lombok.Getter;

/**
 * 图片检索方法枚举
 * @author : Shea.
 * @since : 2026/5/20 21:06
 */
@Getter
public enum ImageMethodEnum {

    PEXELS("PEXELS","Pexels图库",false,false),
    NANO_BANANA("NANO_BANANA","Nano Banana AI生图",true,false),
    MERMAIND("MERMAID","Mermaid AI生图",true,false),
    ICONIFY("ICONIFY","Iconify图标库",false,false),
    EMOJI_PACK("EMOJI_PACK","表情包检索",false,false),
    SVG_DIAGRAM("SVG_DIAGRAM","SVG概念示意图",true,false),
    PICSUM("PICSUM","Picsum 随机图片",false,true);

    private final String value;
    private final String description;
    private final boolean aiGenerated;
    private final boolean fallback;

    ImageMethodEnum(String value, String description, boolean aiGenerated, boolean fallback) {
        this.value = value;
        this.description = description;
        this.aiGenerated = aiGenerated;
        this.fallback = fallback;
    }

    public static ImageMethodEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ImageMethodEnum methodEnum : values()) {
            if (methodEnum.value.equals(value)) {
                return methodEnum;
            }
        }
        return null;
    }

    /**
     * 获取默认的图库检索方式
     */
    public static ImageMethodEnum getDefaultSearchMethod() {
        return PEXELS;
    }

    /**
     * 获取默认的 AI 生图方式
     */
    public static ImageMethodEnum getDefaultAiMethod() {
        return NANO_BANANA;
    }

    /**
     * 获取降级方案
     */
    public static ImageMethodEnum getFallbackMethod() {
        return PICSUM;
    }
}
