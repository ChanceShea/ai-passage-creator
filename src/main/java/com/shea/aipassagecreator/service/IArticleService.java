package com.shea.aipassagecreator.service;


import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.shea.aipassagecreator.domain.dto.ArticleQueryDTO;
import com.shea.aipassagecreator.domain.entity.Article;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.ArticleVO;
import com.shea.aipassagecreator.enums.ArticlePhaseEnum;
import com.shea.aipassagecreator.enums.ArticleStatusEnum;

import java.util.List;

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

    void confirmTitle(String taskId, String mainTitle, String subTitle,String userDescription,User loginUser);

    void confirmOutline(String taskId, List<ArticleState.OutlineSection> outlines,User loginUser);

    void updatePhase(String taskId, ArticlePhaseEnum phase);

    void saveTitleOptions(String taskId,List<ArticleState.TitleOption> titleOptions);

    List<ArticleState.OutlineSection> aiModifyOutline(String taskId,String modifySuggestion,User loginUser);

    String createArticleTaskWithQuotaCheck(String topic,String style, User loginUser);
}
