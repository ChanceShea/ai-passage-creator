package com.shea.aipassagecreator.service;


import com.mybatisflex.core.service.IService;
import com.shea.aipassagecreator.domain.dto.PaymentRecordAddDTO;
import com.shea.aipassagecreator.domain.entity.PaymentRecord;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.PaymentRecordVO;

/**
 * <p>
 * 支付记录表 服务类
 * </p>
 *
 * @author Shea
 * @since 2026-05-30
 */
public interface IPaymentRecordService extends IService<PaymentRecord> {

    PaymentRecordVO addRecord(PaymentRecordAddDTO dto, User loginUser);

    PaymentRecord getByOrderId(String orderId);

    PaymentRecord getByOrderIdForUpdate(String orderId);
}
