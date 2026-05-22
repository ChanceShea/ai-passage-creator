package com.shea.aipassagecreator.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建文章的DTO
 * @author : Shea.
 * @since : 2026/5/21 16:28
 */
@Data
public class ArticleCreateDTO implements Serializable {

    /**
     * 文章选题
     */
    private String topic;

    /**
     * 文章风格
     */
    private String style;
    /**
     * 允许的配图方式列表，为空则表示支持所有方式
     */
    private List<String> enableImageMethods;

    @Serial
    private static final long serialVersionUID = 1L;
}
