package com.shea.aipassagecreator.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 * @author : Shea.
 * @since : 2026/5/30 21:04
 */
@Getter
public enum PaymentStatusEnum {

    PENDING("PENDING","待支付"),
    SUCCEEDED("SUCCEEDED","支付成功"),
    FAILED("FAILED","支付失败"),
    REFUNDED("REFUNDED","已退款");

    private final String value;
    private final String text;

    PaymentStatusEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static PaymentStatusEnum getPaymentStatusEnum(String value) {
        if (value == null) {
            return null;
        }
        for (PaymentStatusEnum paymentStatusEnum : PaymentStatusEnum.values()) {
            if (paymentStatusEnum.value.equals(value)) {
                return paymentStatusEnum;
            }
        }
        return null;
    }
}
