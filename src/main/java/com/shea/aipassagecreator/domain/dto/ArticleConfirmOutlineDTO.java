package com.shea.aipassagecreator.domain.dto;

import com.shea.aipassagecreator.domain.entity.ArticleState;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 确认文章大纲DTO
 * @author : Shea.
 * @since : 2026/5/26 16:35
 */
@Data
public class ArticleConfirmOutlineDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 用户编辑后的大纲
     */
    private List<ArticleState.OutlineSection> outlines;
}
