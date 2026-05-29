package com.shea.aipassagecreator.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * AI修改文章大纲DTO
 * @author : Shea.
 * @since : 2026/5/26 16:45
 */
@Data
public class ArticleAiModifyOutlineDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 修改建议
     */
    private String modifySuggestion;
}
