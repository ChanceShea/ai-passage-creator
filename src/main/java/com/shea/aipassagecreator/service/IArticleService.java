package com.shea.aipassagecreator.service;


import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.shea.aipassagecreator.domain.dto.ArticleQueryDTO;
import com.shea.aipassagecreator.domain.entity.Article;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.ArticleVO;
import com.shea.aipassagecreator.enums.ArticleStatusEnum;

/**
 * <p>
 * 文章表 服务类
 * </p>
 *
 * @author Shea
 * @since 2026-05-20
 */
public interface IArticleService extends IService<Article> {

    Article getByTaskId(String taskId);

    void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage);

    String createArticleTask(String topic, String style, User loginUser);

    void saveArticleContent(String taskId, ArticleState state);

    ArticleVO getArticleDetail(String taskId, User loginUser);

    Page<ArticleVO> listArticleByPage(ArticleQueryDTO dto, User loginUser);

    boolean deleteArticle(String id, User loginUser);

    String createArticleTaskWithQuotaCheck(String topic,String style, User loginUser);
}
