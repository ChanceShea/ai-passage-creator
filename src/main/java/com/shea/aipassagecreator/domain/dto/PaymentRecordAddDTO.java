package com.shea.aipassagecreator.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付记录添加DTO
 * @author : Shea.
 * @since : 2026/5/31 12:51
 */
@Data
public class PaymentRecordAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String currency;

    private String description;

    private BigDecimal amount;

    private String paymentType;

    private String productType;
}
