package com.shea.aipassagecreator.controller;


import com.shea.aipassagecreator.common.Result;
import com.shea.aipassagecreator.domain.dto.PaymentRecordAddDTO;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.PaymentRecordVO;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.service.IPaymentRecordService;
import com.shea.aipassagecreator.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.shea.aipassagecreator.exception.ThrowUtils.throwIf;

/**
 * <p>
 * 支付记录表 前端控制器
 * </p>
 *
 * @author Shea
 * @since 2026-05-30
 */
@RestController
@RequestMapping("/payment-record")
@RequiredArgsConstructor
public class PaymentRecordController {

    private final IPaymentRecordService paymentRecordService;
    private final IUserService userService;

    @GetMapping("/create")
    public Result<PaymentRecordVO> createPaymentRecord(PaymentRecordAddDTO dto,HttpServletRequest request) {
        throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        PaymentRecordVO vo = paymentRecordService.addRecord(dto, loginUser);
        return Result.success(vo);
    }


}
