package com.shea.aipassagecreator.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 文章风格枚举
 * @author : Shea.
 * @since : 2026/5/25 14:01
 */
@Getter
public enum ArticleStyleEnum {

    TECH("tech","科技风格"),
    EMOTIONAL("emotional","情感风格"),
    EDUCATIONAL("educational","教育风格"),
    HUMOROUS("humorous","幽默风格");

    private final String value;
    private final String text;

    ArticleStyleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 获取所有值
     * @return 所有值
     */
    public static List<String> getValues() {
        return Arrays.stream(values())
                .map(ArticleStyleEnum::getValue)
                .toList();
    }

    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static ArticleStyleEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ArticleStyleEnum articleStyleEnum : values()) {
            if (articleStyleEnum.getValue().equals(value)) {
                return articleStyleEnum;
            }
        }
        return null;
    }

    /**
     * 判断值是否有效
     * @param value 值
     * @return 是否有效
     */
    public static boolean isValid(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return getEnumByValue(value) != null;
    }
}
