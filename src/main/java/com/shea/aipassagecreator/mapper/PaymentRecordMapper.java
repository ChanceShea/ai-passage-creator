package com.shea.aipassagecreator.mapper;


import com.mybatisflex.core.BaseMapper;
import com.shea.aipassagecreator.domain.entity.PaymentRecord;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 支付记录表 Mapper 接口
 * </p>
 *
 * @author Shea
 * @since 2026-05-30
 */
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

    @Select("SELECT * FROM payment_record WHERE orderId = #{orderId} FOR UPDATE")
    PaymentRecord selectByOrderIdForUpdate(String orderId);

}
