package com.shea.aipassagecreator.service;

import java.util.Map;

/**
 * 支付服务
 * @author : Shea.
 * @since : 2026/5/30 21:46
 */
public interface IAlipayService {
    String pay(String orderId, Long userId) throws Exception;

    void payNotify(Map<String, String[]> result);
}
