package com.shea.aipassagecreator.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付记录表VO
 * @author : Shea.
 * @since : 2026/5/31 13:00
 */
@Data
public class PaymentRecordVO {

    private String userId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String productType;

}
