package com.shea.aipassagecreator.service.impl;

import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.shea.aipassagecreator.config.AlipayConfiguration;
import com.shea.aipassagecreator.constant.UserConstant;
import com.shea.aipassagecreator.domain.entity.PaymentRecord;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.enums.PaymentStatusEnum;
import com.shea.aipassagecreator.service.IAlipayService;
import com.shea.aipassagecreator.service.IPaymentRecordService;
import com.shea.aipassagecreator.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 支付实现类
 * @author : Shea.
 * @since : 2026/5/30 21:46
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlipayServiceImpl implements IAlipayService {

    private final AlipayConfiguration alipayConfiguration;
    private final IUserService userService;
    private final IPaymentRecordService paymentRecordService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String pay(String orderId,Long userId) throws Exception {
        User user = userService.getById(userId);
        if (UserConstant.VIP_ROLE.equals(user.getUserRole())) {
            return "您已是会员，无法重复购买";
        }
        // 根据订单编号获取支付记录
        PaymentRecord payment = paymentRecordService.getByOrderId(orderId);
        if (PaymentStatusEnum.SUCCEEDED.getValue().equals(payment.getStatus())) {
            return "订单已支付，请勿重复支付";
        }
        // 创建支付宝客户端
        AlipayClient client = new DefaultAlipayClient(alipayConfiguration.getAlipayConfig());
        // 创建支付请求
        AlipayTradePagePayRequest alipayTradePagePayRequest = getAlipayTradePagePayRequest(userId, payment);
        String result = client.pageExecute(alipayTradePagePayRequest).getBody();
        return result;
    }

    /**
     * 获取支付宝支付请求
     * @param userId 用户Id
     * @param payment 支付记录
     * @return 支付请求
     */
    @NotNull
    private AlipayTradePagePayRequest getAlipayTradePagePayRequest(Long userId, PaymentRecord payment) {
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
        // 设置支付回调地址
        alipayTradePagePayRequest.setNotifyUrl(alipayConfiguration.getNotifyUrl());
        // 设置支付成功返回地址
        alipayTradePagePayRequest.setReturnUrl(alipayConfiguration.getReturnUrl());
        // 设置支付请求参数
        JSONObject jsonObject = new JSONObject();
        // 设置业务参数
        Map<String,String> map = new HashMap<>();
        map.put("out_trade_no", payment.getOrderId());
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", String.valueOf(payment.getAmount()));
        map.put("subject", payment.getDescription());
        map.put("passback_params", String.valueOf(userId));
        jsonObject.putAll(map);
        alipayTradePagePayRequest.setBizContent(jsonObject.toString());
        return alipayTradePagePayRequest;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payNotify(Map<String, String[]> result) {
        if (!verifyAlipaySignature(result)) {
            log.error("支付宝签名验证失败");
            throw new RuntimeException("支付宝签名验证失败");
        }
        // 支付流水号
        String no = result.get("trade_no")[0];
        // 订单编号
        String orderId = result.get("out_trade_no")[0];
        // 用户Id
        String userId = result.get("passback_params")[0];
        // 交易状态
        String tradeStatus = result.get("trade_status")[0];
        log.info("支付宝回调通知，订单编号：{}，支付流水号：{}，用户Id：{}，交易状态：{}", orderId, no, userId, tradeStatus);
        // 幂等性处理，防止重复通知
        String lockKey = "alipay:notify:" + orderId;
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(locked)) {
            log.warn("支付宝回调通知，订单编号：{}，支付流水号：{}，用户Id：{}，交易状态：{}，订单已处理，请勿重复处理",
                    orderId, no, userId, tradeStatus);
            return;
        }
        try {
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 更新支付记录
                // 使用悲观锁更新支付记录，防止并发修改
                PaymentRecord record = paymentRecordService.getByOrderIdForUpdate(orderId);
                if (record == null) {
                    throw new RuntimeException("支付记录不存在");
                }
                // 幂等性，不重复处理订单
                if (PaymentStatusEnum.SUCCEEDED.getValue().equals(record.getStatus())) {
                    log.warn("订单:{}已处理成功",orderId);
                    return;
                }
                record.setTradeNo(no);
                record.setUserId(Long.valueOf(userId));
                record.setStatus(PaymentStatusEnum.SUCCEEDED.getValue());
                paymentRecordService.updateById(record);
                // 更新用户状态
                User user = userService.getById(userId);
                if (user == null) {
                    throw new RuntimeException("用户不存在");
                }
                user.setUserRole(UserConstant.VIP_ROLE);
                user.setVipTime(LocalDateTime.now());
                userService.updateById(user);
                log.info("用户:{}已升级为会员", userId);
                log.info("订单:{}已处理成功", orderId);
            }
        } catch (Exception e) {
            log.info("订单:{}处理失败",orderId,e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 验证支付宝签名
     * @param params 参数
     * @return 是否验证通过
     */
    private boolean verifyAlipaySignature(Map<String,String[]> params) {
        try {
            Map<String, String> map = new HashMap<>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                map.put(entry.getKey(), entry.getValue()[0]);
            }
            return AlipaySignature.rsaCheckV1(
                    map,
                    alipayConfiguration.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );
        }catch (Exception e) {
            log.error("支付宝签名验证失败", e);
            return false;
        }
    }
}
