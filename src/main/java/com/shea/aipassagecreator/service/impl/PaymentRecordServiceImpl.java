package com.shea.aipassagecreator.service.impl;


import com.mybatisflex.core.keygen.impl.FlexIDKeyGenerator;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shea.aipassagecreator.constant.UserConstant;
import com.shea.aipassagecreator.domain.dto.PaymentRecordAddDTO;
import com.shea.aipassagecreator.domain.entity.PaymentRecord;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.PaymentRecordVO;
import com.shea.aipassagecreator.enums.PaymentStatusEnum;
import com.shea.aipassagecreator.enums.PaymentTypeEnum;
import com.shea.aipassagecreator.enums.ProductTypeEnum;
import com.shea.aipassagecreator.exception.BusinessException;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.mapper.PaymentRecordMapper;
import com.shea.aipassagecreator.service.IPaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.shea.aipassagecreator.exception.ThrowUtils.throwIf;

/**
 * <p>
 * 支付记录表 服务实现类
 * </p>
 *
 * @author Shea
 * @since 2026-05-30
 */
@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements IPaymentRecordService {

    private final PaymentRecordMapper paymentRecordMapper;

    /**
     * 添加支付记录
     *
     * @param dto       支付记录添加DTO
     * @param loginUser 登录用户
     * @return 支付记录VO
     */
    @Override
    public PaymentRecordVO addRecord(PaymentRecordAddDTO dto, User loginUser) {
        Long id = loginUser.getId();
        if (UserConstant.VIP_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已经是会员，不能重复购买");
        }
        // 获取支付方式
        String paymentType = dto.getPaymentType();
        PaymentTypeEnum paymentTypeEnum = PaymentTypeEnum.getByValue(paymentType);
        if (paymentTypeEnum == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的支付方式：" + paymentType);
        }
        // 获取产品类型
        String productType = dto.getProductType();
        ProductTypeEnum productTypeEnum = ProductTypeEnum.getByValue(productType);
        if (productTypeEnum == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的产品类型：" + productType);
        }
        // 获取订单号
        FlexIDKeyGenerator flexIDKeyGenerator = new FlexIDKeyGenerator();
        String orderId = flexIDKeyGenerator.generate(null, null).toString();
        PaymentRecord paymentRecord = PaymentRecord.builder()
                .userId(id)
                .orderId(orderId)
                .amount(dto.getAmount())
                .paymentType(paymentTypeEnum.getValue())
                .description(dto.getDescription())
                .productType(productTypeEnum.getValue())
                .status(PaymentStatusEnum.PENDING.getValue())
                .build();
        boolean save = this.save(paymentRecord);
        throwIf(!save, ErrorCode.OPERATION_ERROR, "支付记录添加失败");
        PaymentRecordVO vo = new PaymentRecordVO();
        BeanUtils.copyProperties(paymentRecord, vo);
        return vo;
    }

    /**
     * 根据订单号获取支付记录
     * @param orderId 订单号
     * @return 支付记录
     */
    @Override
    public PaymentRecord getByOrderId(String orderId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("orderId", orderId);
        return this.getOne(queryWrapper);
    }

    @Override
    public PaymentRecord getByOrderIdForUpdate(String orderId) {
        return paymentRecordMapper.selectByOrderIdForUpdate(orderId);
    }
}
