package com.shea.aipassagecreator.service.impl;


import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shea.aipassagecreator.domain.dto.ArticleQueryDTO;
import com.shea.aipassagecreator.domain.entity.Article;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.ArticleVO;
import com.shea.aipassagecreator.enums.ArticlePhaseEnum;
import com.shea.aipassagecreator.enums.ArticleStatusEnum;
import com.shea.aipassagecreator.exception.BusinessException;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.mapper.ArticleMapper;
import com.shea.aipassagecreator.service.ArticleAgentService;
import com.shea.aipassagecreator.service.IArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.shea.aipassagecreator.constant.UserConstant.ADMIN_ROLE;
import static com.shea.aipassagecreator.exception.ThrowUtils.throwIf;

/**
 * <p>
 * 文章表 服务实现类
 * </p>
 *
 * @author Shea
 * @since 2026-05-20
 */
@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    private final ArticleAgentService articleAgentService;

    public ArticleServiceImpl(ArticleAgentService articleAgentService) {
        this.articleAgentService = articleAgentService;
    }

    @Override
    public Article getByTaskId(String taskId) {
        return this.getOne(QueryWrapper.create().eq("taskId", taskId));
    }

    @Override
    public void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage) {
        Article article = getByTaskId(taskId);
        if (article == null) {
            log.error("文章记录不存在，taskId={}", taskId);
            return;
        }

        article.setStatus(status.getValue());
        article.setErrorMessage(errorMessage);
        this.updateById(article);
        log.info("文章状态更新完成，taskId={},status={}", taskId, status.getValue());
    }

    @Override
    public String createArticleTask(String topic, String style, User loginUser) {
        String taskId = IdUtil.simpleUUID();

        Article article = new Article();
        article.setTaskId(taskId);
        article.setTopic(topic);
        article.setUserId(loginUser.getId());
        article.setStyle(style);
        article.setStatus(ArticleStatusEnum.PENDING.getValue());
        article.setCreateTime(LocalDateTime.now());

        this.save(article);
        log.info("文章任务创建完成，taskId={},userId={}", taskId,loginUser.getId());
        return taskId;
    }

    /**
     * 保存文章内容，文章生成完成后，将文章内容保存到数据库中
     * @param taskId 文章任务ID
     * @param state 文章状态
     */
    @Override
    public void saveArticleContent(String taskId, ArticleState state) {
        Article article = getByTaskId(taskId);
        if (article == null) {
            log.error("文章记录不存在，taskId={}", taskId);
            return;
        }
        article.setMainTitle(state.getTitle().getMainTitle());
        article.setSubTitle(state.getTitle().getSubTitle());
        article.setOutline(JSONUtil.toJsonStr(state.getOutline().getSections()));
        article.setContent(state.getContent());
        article.setFullContent(state.getFullContent());

        if (state.getImages() != null && !state.getImages().isEmpty()) {
            ArticleState.ImageResult cover = state.getImages().stream()
                    .filter(img -> img.getPosition() != null && img.getPosition() == 1)
                    .findFirst()
                    .orElse(null);
            if (cover != null && cover.getUrl() != null) {
                article.setCoverImage(cover.getUrl());
            }
        }
        article.setImages(JSONUtil.toJsonStr(state.getImages()));
        article.setCompletedTime(LocalDateTime.now());

        this.updateById(article);
        log.info("文章内容保存完成，taskId={}", taskId);
    }

    @Override
    public ArticleVO getArticleDetail(String taskId, User loginUser) {
        Article article = getByTaskId(taskId);
        throwIf(article == null,new BusinessException(ErrorCode.NOT_FOUND_ERROR,"文章不存在"));
        checkArticlePermission(article,loginUser);
        return ArticleVO.objTOVo(article);
    }

    @Override
    public Page<ArticleVO> listArticleByPage(ArticleQueryDTO dto, User loginUser) {
        QueryWrapper eq = QueryWrapper.create()
                .eq("isDelete", 0)
                .orderBy("createTime", false);
        if (!ADMIN_ROLE.equals(loginUser.getUserRole())) {
            eq.eq("userId", loginUser.getId());
        } else if (dto.getUserId() != null){
            eq.eq("userId", dto.getUserId());
        }

        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            eq.eq("status", dto.getStatus());
        }
        Page<Article> page = this.page(new Page<>(dto.getPage(), dto.getSize()), eq);
        return convertToVOPage(page);
    }

    @Override
    public boolean deleteArticle(String id, User loginUser) {
        Article byId = this.getById(id);
        if (byId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }
        checkArticlePermission(byId,loginUser);
        return this.removeById(id);
    }

    /**
     * 确认文章标题
     * @param taskId 任务ID
     * @param mainTitle 主标题
     * @param subTitle 副标题
     * @param userDescription 用户补充描述
     * @param loginUser 当前登录用户
     */
    @Override
    public void confirmTitle(String taskId, String mainTitle, String subTitle, String userDescription, User loginUser) {
        Article article = getByTaskId(taskId);
        throwIf(article == null,new BusinessException(ErrorCode.NOT_FOUND_ERROR,"文章不存在"));
        // 校验权限
        checkArticlePermission(article,loginUser);
        // 校验当前阶段必须是TITLE_SELECTING
        ArticlePhaseEnum phase = ArticlePhaseEnum.getByValue(article.getPhase());
        throwIf(phase != ArticlePhaseEnum.TITLE_SELECTING,new BusinessException(ErrorCode.OPERATION_ERROR,"当前阶段不允许此操作"));
        // 保存用户选的标题和补充描述
        article.setMainTitle(mainTitle);
        article.setSubTitle(subTitle);
        article.setUserDescription(userDescription);
        article.setPhase(ArticlePhaseEnum.OUTLINE_GENERATING.getValue());
        this.updateById(article);
        log.info("用户确认标题，taskId={},mainTitle={}",taskId,mainTitle);
    }

    /**
     * 确认文章大纲
     * @param taskId 任务ID
     * @param outlines 大纲
     * @param loginUser 当前登录用户
     */
    @Override
    public void confirmOutline(String taskId, List<ArticleState.OutlineSection> outlines, User loginUser) {
        Article article = getByTaskId(taskId);
        throwIf(article == null,new BusinessException(ErrorCode.NOT_FOUND_ERROR,"文章不存在"));
        // 校验权限
        checkArticlePermission(article,loginUser);
        ArticlePhaseEnum phase = ArticlePhaseEnum.getByValue(article.getPhase());
        throwIf(phase != ArticlePhaseEnum.OUTLINE_EDITING,ErrorCode.OPERATION_ERROR,"当前阶段不允许此操作");
        // 保存用户编辑后的大纲
        article.setOutline(JSONUtil.toJsonStr(outlines));
        article.setPhase(ArticlePhaseEnum.CONTENT_GENERATING.getValue());
        this.updateById(article);
        log.info("用户确认大纲，taskId={},sectionCount={}",taskId,outlines.size());
    }

    /**
     * 更新文章阶段
     * @param taskId 任务ID
     * @param phase 阶段
     */
    @Override
    public void updatePhase(String taskId, ArticlePhaseEnum phase) {
        Article article = getByTaskId(taskId);
        if (article == null) {
            log.error("文章记录不存在，taskId={}", taskId);
            return;
        }
        article.setPhase(phase.getValue());
        this.updateById(article);
        log.info("文章阶段更新完成，taskId={},phase={}", taskId, phase.getValue());
    }

    /**
     * 保存文章标题选项
     * @param taskId 任务ID
     * @param titleOptions 标题选项
     */
    @Override
    public void saveTitleOptions(String taskId, List<ArticleState.TitleOption> titleOptions) {
        Article article = getByTaskId(taskId);
        if (article == null) {
            log.error("文章记录不存在，taskId={}", taskId);
            return;
        }
        article.setTitleOptions(JSONUtil.toJsonStr(titleOptions));
        this.updateById(article);
        log.info("文章标题选项保存完成，taskId={},optionsCount={}", taskId, titleOptions.size());
    }

    /**
     * AI修改文章大纲
     * @param taskId 任务ID
     * @param modifySuggestion 修改建议
     * @param loginUser 当前登录用户
     * @return 修改后的文章大纲
     */
    @Override
    public List<ArticleState.OutlineSection> aiModifyOutline(String taskId, String modifySuggestion, User loginUser) {
        Article article = getByTaskId(taskId);
        throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        // 校验权限
        checkArticlePermission(article,loginUser);
        ArticlePhaseEnum phase = ArticlePhaseEnum.getByValue(article.getPhase());
        throwIf(phase != ArticlePhaseEnum.OUTLINE_EDITING,ErrorCode.OPERATION_ERROR,"当前阶段不允许此操作");
        List<ArticleState.OutlineSection> currentOutline = JSONUtil.toList(article.getOutline(), ArticleState.OutlineSection.class);
        List<ArticleState.OutlineSection> modifyOutline = articleAgentService.aiModifyOutline(article.getMainTitle(), article.getSubTitle(), currentOutline, modifySuggestion);
        article.setOutline(JSONUtil.toJsonStr(modifyOutline));
        this.updateById(article);
        log.info("AI修改文章大纲完成，taskId={},sectionsCount={}", taskId,modifyOutline.size());
        return modifyOutline;
    }

    /**
     * 创建文章任务并检查配额
     * @param topic 文章主题
     * @param style 文章风格
     * @param loginUser 当前登录用户
     * @return 任务ID
     */
    @Override
    public String createArticleTaskWithQuotaCheck(String topic, String style, User loginUser) {
        return null;
    }

    /**
     * 将文章列表转换为文章VO列表
     * @param page 文章列表
     * @return 文章VO列表
     */
    private Page<ArticleVO> convertToVOPage(Page<Article> page) {
        List<ArticleVO> list = page.getRecords().stream().map(ArticleVO::objTOVo).toList();
        Page<ArticleVO> voPage = new Page<>();
        voPage.setRecords(list);
        voPage.setPageNumber(page.getPageNumber());
        voPage.setPageSize(page.getPageSize());
        voPage.setTotalPage(page.getTotalPage());
        voPage.setTotalRow(page.getTotalRow());
        return voPage;
    }

    /**
     * 检查文章权限
     * @param article 文章
     * @param loginUser 登录用户
     */
    private void checkArticlePermission(Article article, User loginUser) {
        if (!article.getUserId().equals(loginUser.getId()) && !ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}
