package com.shea.aipassagecreator.controller;


import com.shea.aipassagecreator.common.Result;
import com.shea.aipassagecreator.domain.dto.UserRegisterDTO;
import com.shea.aipassagecreator.exception.BusinessException;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @PostMapping("/user/register")
    public Result<Long> register(UserRegisterDTO dto) {
        throwIf(dto == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        throwIf(dto.getUserAccount() == null || dto.getUserPassword() == null || dto.getCheckPassword() == null, new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码不能为空"));
        return Result.success(userService.register(dto.getUserAccount(),dto.getUserPassword(),dto.getCheckPassword()));
    }
}
