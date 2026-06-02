package com.shea.aipassagecreator.mapper;


import com.mybatisflex.core.BaseMapper;
import com.shea.aipassagecreator.domain.entity.User;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 用户 Mapper 接口
 * </p>
 *
 * @author Shea
 * @since 2026-05-18
 */
public interface UserMapper extends BaseMapper<User> {

    @Update("update user set quota = quota - 1 where id = #{id} and quota > 0")
    int decrementQuota(Long id);
}
