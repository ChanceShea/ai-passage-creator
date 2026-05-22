package com.shea.aipassagecreator.enums;

import lombok.Getter;

/**
 * 文章状态枚举
 * @author : Shea.
 * @since : 2026/5/21 09:45
 */
@Getter
public enum ArticleStatusEnum {

    PENDING("PENDING","待处理"),
    PROCESSING("PROCESSING","处理中"),
    COMPLETED("COMPLETED","已完成"),
    FAILED("FAILED","失败");

    private final String value;
    private final String description;

    ArticleStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ArticleStatusEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ArticleStatusEnum enums : values()) {
            if (enums.getValue().equals(value)) {
                return enums;
            }
        }
        return null;
    }
}
