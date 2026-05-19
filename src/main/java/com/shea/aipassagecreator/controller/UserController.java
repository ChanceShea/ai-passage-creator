package com.shea.aipassagecreator.controller;


import com.shea.aipassagecreator.annotation.AuthCheck;
import com.shea.aipassagecreator.common.DeleteRequest;
import com.shea.aipassagecreator.common.Result;
import com.shea.aipassagecreator.constant.UserConstant;
import com.shea.aipassagecreator.domain.dto.UserAddDTO;
import com.shea.aipassagecreator.domain.dto.UserLoginDTO;
import com.shea.aipassagecreator.domain.dto.UserRegisterDTO;
import com.shea.aipassagecreator.domain.vo.LoginUserVO;
import com.shea.aipassagecreator.exception.BusinessException;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.shea.aipassagecreator.exception.ThrowUtils.throwIf;

/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author Shea
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 用户注册
     * @param dto 用户注册参数
     * @return 用户注册结果
     */
    @PostMapping("/user/register")
    public Result<Long> register(@RequestBody UserRegisterDTO dto) {
        throwIf(dto == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        throwIf(dto.getUserAccount() == null || dto.getUserPassword() == null || dto.getCheckPassword() == null, new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码不能为空"));
        return Result.success(userService.register(dto.getUserAccount(),dto.getUserPassword(),dto.getCheckPassword()));
    }

    /**
     * 用户登录
     * @param dto 用户登录参数
     * @return 用户登录结果
     */
    @PostMapping("/user/login")
    public Result<LoginUserVO> login(@RequestBody UserLoginDTO dto, HttpServletRequest request) {
        throwIf(dto.getUserAccount() == null || dto.getUserPassword() == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        return Result.success(userService.login(dto.getUserAccount(), dto.getUserPassword(), request));
    }

    /**
     * 获取当前登录用户
     * @return 当前登录用户
     */
    @GetMapping("/user/get/login")
    public Result<LoginUserVO> getLoginUser(HttpServletRequest request) {
        return Result.success(userService.getLoginUserVO(request));
    }

    /**
     * 用户注销
     * @param request 请求
     * @return 注销结果
     */
    @GetMapping("/user/logout")
    public Result<Boolean> logout(HttpServletRequest request) {
        return Result.success(userService.logout(request));
    }

    @PostMapping("/user/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> addUser(@RequestBody UserAddDTO dto) {
        throwIf(dto == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        throwIf(dto.getUserAccount() == null || dto.getUserPassword() == null, new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码不能为空"));
        return Result.success(userService.addUser(dto));
    }

    @DeleteMapping("/user/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> delete(@RequestBody DeleteRequest request) {
        throwIf(request == null || request.getId() == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        return Result.success(userService.removeById(request.getId()));
    }
}
