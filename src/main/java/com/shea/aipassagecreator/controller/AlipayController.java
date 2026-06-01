package com.shea.aipassagecreator.controller;

import com.shea.aipassagecreator.config.AlipayConfiguration;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.service.IAlipayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.shea.aipassagecreator.exception.ThrowUtils.throwIf;

/**
 * 支付宝支付Controller
 * @author : Shea.
 * @since : 2026/5/30 21:01
 */
@RestController
@RequestMapping("/alipay")
@RequiredArgsConstructor
@Slf4j
public class AlipayController {

    private final IAlipayService alipayService;
    private final AlipayConfiguration alipayConfiguration;

    @GetMapping("/pay")
    public void pay(
            @RequestParam("orderId") String orderId,
            @RequestParam("userId") Long userId,
            HttpServletResponse response
    ) throws Exception {
        throwIf(orderId == null || userId == null, ErrorCode.PARAMS_ERROR);
        String pay = alipayService.pay(orderId,userId);
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(pay);
        response.getWriter().flush();
        response.getWriter().close();
    }

    @GetMapping("/return")
    public void returnPay(HttpServletResponse response) throws Exception {
        // 设置字符编码为utf-8
        response.setCharacterEncoding("utf-8");
        // 设置相应类型为HTML，并指定字符编码
        response.setContentType("text/html;charset=utf-8");
        // 重定向到指定的HTML页面
        response.sendRedirect(alipayConfiguration.getSuccessUrl());
    }

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) throws Exception {
        Map<String, String[]> result = request.getParameterMap();
        try {
            alipayService.payNotify(result);
            return "success";
        }catch (Exception e) {
            log.error("支付处理失败",e);
            return "fail";
        }
    }

}
