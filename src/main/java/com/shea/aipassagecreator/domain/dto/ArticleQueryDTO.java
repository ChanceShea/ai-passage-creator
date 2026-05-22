package com.shea.aipassagecreator.domain.dto;

import com.shea.aipassagecreator.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文章查询参数
 * @author : Shea.
 * @since : 2026/5/21 19:09
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class ArticleQueryDTO extends PageRequest implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 状态
     */
    private String status;

    @Serial
    private static final long serialVersionUID = 1L;
}
