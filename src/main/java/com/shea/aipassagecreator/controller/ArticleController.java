package com.shea.aipassagecreator.controller;


import com.mybatisflex.core.paginate.Page;
import com.shea.aipassagecreator.annotation.AuthCheck;
import com.shea.aipassagecreator.common.DeleteRequest;
import com.shea.aipassagecreator.common.Result;
import com.shea.aipassagecreator.constant.UserConstant;
import com.shea.aipassagecreator.domain.dto.ArticleCreateDTO;
import com.shea.aipassagecreator.domain.dto.ArticleQueryDTO;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.ArticleVO;
import com.shea.aipassagecreator.exception.BusinessException;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.manager.SseEmitterManager;
import com.shea.aipassagecreator.service.ArticleAsyncService;
import com.shea.aipassagecreator.service.IArticleService;
import com.shea.aipassagecreator.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static com.shea.aipassagecreator.exception.ThrowUtils.throwIf;

/**
 * <p>
 * 文章表 前端控制器
 * </p>
 *
 * @author Shea
 * @since 2026-05-20
 */
@RestController
@RequestMapping("/article")
@Slf4j
public class ArticleController {

    @Resource
    private IArticleService articleService;
    @Resource
    private ArticleAsyncService articleAsyncService;
    @Resource
    private SseEmitterManager sseEmitterManager;
    @Resource
    private IUserService userService;

    /**
     * 创建文章
     * @param dto 创建文章的DTO
     * @param request HTTP请求
     * @return 创建文章的任务ID
     */
    @PostMapping("/create")
    public Result<String> createArticle(@RequestBody ArticleCreateDTO dto, HttpServletRequest request) {
        throwIf(dto == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        throwIf(dto.getTopic() == null || dto.getTopic().trim().isEmpty(),new BusinessException(ErrorCode.PARAMS_ERROR,"文章选题不能为空"));

        User loginUser = userService.getLoginUser(request);
        String taskId = articleService.createArticleTask(dto.getTopic(),loginUser);
        articleAsyncService.executeArticleGeneration(taskId, dto.getTopic());
        return Result.success(taskId);
    }

    /**
     * 获取文章生成进度
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 文章生成进度
     */
    @GetMapping("/progress/{taskId}")
    public SseEmitter getProgress(@PathVariable("taskId") String taskId,HttpServletRequest request) {
        throwIf(taskId == null, new BusinessException(ErrorCode.PARAMS_ERROR,"任务ID不能为空"));
        User loginUser = userService.getLoginUser(request);
        articleService.getArticleDetail(taskId,loginUser);
        SseEmitter sseEmitter = sseEmitterManager.createEmitter(taskId);
        log.info("SSE连接已建立，taskId={}", taskId);
        return sseEmitter;
    }

    /**
     * 获取文章详情
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 文章详情
     */
    @GetMapping("/{taskId}")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public Result<ArticleVO> getArticle(@PathVariable("taskId") String taskId,HttpServletRequest request) {
        throwIf(taskId == null || taskId.trim().isEmpty(), new BusinessException(ErrorCode.PARAMS_ERROR,"任务Id不能为空"));

        User loginUser = userService.getLoginUser(request);
        ArticleVO articleVO = articleService.getArticleDetail(taskId, loginUser);
        return Result.success(articleVO);
    }

    /**
     * 分页获取文章列表
     * @param dto 查询文章的DTO
     * @param request HTTP请求
     * @return 文章列表
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public Result<Page<ArticleVO>> listArticle(@RequestBody ArticleQueryDTO dto,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ArticleVO> articleVOPage = articleService.listArticleByPage(dto, loginUser);
        return Result.success(articleVOPage);
    }

    /**
     * 删除文章
     * @param request 删除文章的请求
     * @param httpServletRequest HTTP请求
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public Result<Boolean> deleteArticle(@RequestBody DeleteRequest request,HttpServletRequest httpServletRequest) {
        throwIf(request == null || request.getId() == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean res = articleService.deleteArticle(request.getId(), loginUser);
        return Result.success(res);
    }
}
