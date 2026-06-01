package com.shea.aipassagecreator.config;

import com.alipay.api.AlipayConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 * @author : Shea.
 * @since : 2026/5/30 20:56
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayConfiguration {
    /**
     * 支付宝appid
     */
    private String appId;
    /**
     * 私钥
     */
    private String privateKey;
    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;
    /**
     * 通知地址
     */
    private String notifyUrl;
    /**
     * 回调地址
     */
    private String returnUrl;
    /**
     * 网关地址
     */
    private String gateWayUrl;
    /**
     * 支付成功地址
     */
    private String successUrl;

    /**
     * 获取支付宝配置
     * @return 支付宝配置
     */
    public AlipayConfig getAlipayConfig() {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setAppId(this.appId);
        alipayConfig.setPrivateKey(this.privateKey);
        alipayConfig.setAlipayPublicKey(this.alipayPublicKey);
        alipayConfig.setServerUrl(this.gateWayUrl);
        alipayConfig.setFormat("json");
        alipayConfig.setCharset("utf-8");
        alipayConfig.setSignType("RSA2");
        return alipayConfig;
    }
}
