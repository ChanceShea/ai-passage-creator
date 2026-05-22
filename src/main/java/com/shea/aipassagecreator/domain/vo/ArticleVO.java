package com.shea.aipassagecreator.domain.vo;

import cn.hutool.json.JSONUtil;
import com.shea.aipassagecreator.domain.entity.Article;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章VO
 * @author : Shea.
 * @since : 2026/5/21 16:48
 */
@Data
public class ArticleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String taskId;

    private Long userId;

    private String topic;

    private String userDescription;

    private String mainTitle;

    private String subTitle;

    private List<TitleOption> titleOptions;

    private List<OutlineItem> outline;

    private String content;

    private String fullContent;

    private String coverImage;

    private List<ImageItem> images;

    private String status;

    private String phase;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime completedTime;


    @Data
    public static class TitleOption implements Serializable {
        private String mainTitle;
        private String subTitle;
    }

    @Data
    public static class OutlineItem implements Serializable {
        private Integer section;
        private String title;
        private List<String> points;
    }

    @Data
    public static class ImageItem implements Serializable {
        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;
    }

    public static ArticleVO objTOVo(Article article) {
        if (article == null) {
            return null;
        }
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);

        if (article.getTitleOptions() != null) {
            vo.setTitleOptions(JSONUtil.toList(article.getTitleOptions(), TitleOption.class));
        }
        if (article.getOutline() != null) {
            vo.setOutline(JSONUtil.toList(article.getOutline(), OutlineItem.class));
        }
        if (article.getImages() != null) {
            vo.setImages(JSONUtil.toList(article.getImages(), ImageItem.class));
        }
        return vo;
    }
}
