package com.shea.aipassagecreator.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 确认文章标题DTO
 * @author : Shea.
 * @since : 2026/5/26 16:34
 */
@Data
public class ArticleConfirmTitleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 选中的主标题
     */
    private String selectedMainTitle;

    /**
     * 选中的副标题
     */
    private String selectedSubTitle;

    /**
     * 用户描述
     */
    private String userDescription;
}
