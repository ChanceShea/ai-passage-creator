package com.shea.aipassagecreator.enums;

import lombok.Getter;

/**
 * 文章处理阶段枚举
 * @author : Shea.
 * @since : 2026/5/26 16:30
 */
@Getter
public enum ArticlePhaseEnum {

    PENDING("PENDING","等待处理"),
    TITLE_GENERATING("TITLE_GENERATING","生成标题中"),
    TITLE_SELECTING("TITLE_SELECTING","等待选择标题"),
    OUTLINE_GENERATING("OUTLINE_GENERATING","生成大纲中"),
    OUTLINE_EDITING("OUTLINE_EDITING","等待编辑大纲"),
    CONTENT_GENERATING("CONTENT_GENERATING","生成正文中");

    private final String value;
    private final String text;


    ArticlePhaseEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static ArticlePhaseEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ArticlePhaseEnum articlePhaseEnum : ArticlePhaseEnum.values()) {
            if (articlePhaseEnum.getValue().equals(value)) {
                return articlePhaseEnum;
            }
        }
        return null;
    }
}
