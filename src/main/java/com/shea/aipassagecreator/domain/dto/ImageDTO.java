package com.shea.aipassagecreator.domain.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 图片请求对象
 * @author : Shea.
 * @since : 2026/5/24 09:41
 */
@Data
@Builder
public class ImageDTO {

    /**
     * 关键字（图库检索）
     */
    private String keywords;
    /**
     * 提示词（AI生图）
     */
    private String prompt;
    /**
     * 图片位置序号
     */
    private Integer position;
    /**
     * 图片类型（cover/section）
     */
    private String type;
    /**
     * 宽高比（16:9 4:3）
     */
    private String aspectRatio;
    /**
     * 图片风格描述
     */
    private String style;

    /**
     * 获取有效参数，AI生图优先使用prompt，图片检索优先使用keywords
     * @param isAiGenerated 是否为AI生成
     * @return 有效参数
     */
    public String getEffectiveParam(boolean isAiGenerated) {
        if (isAiGenerated) {
            return prompt != null && !prompt.isEmpty() ? prompt : keywords;
        }
        return keywords != null && !keywords.isEmpty() ? keywords : prompt;
    }
}
