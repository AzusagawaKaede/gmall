package com.atguigu.gmall.cart.mapper;

import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author: rlk
 * @date: 2022/8/9
 * Description: CartInfo的mapper接口
 */
@Mapper
public interface CartInfoMapper extends BaseMapper<CartInfo> {

    /**
     * 根据userId修改是否选中
     * @param username
     * @param status
     * @return
     */
    @Update("update cart_info set is_checked = #{status} where user_id = #{username}")
    int updateAll(@Param("username") String username, @Param("status") Short status);

    /**
     * 根据userId和cartInfoId修改是否选中
     * @param username
     * @param cartInfoId
     * @param status
     * @return
     */
    @Update("update cart_info set is_checked = #{status} where user_id = #{username} and id = #{cartInfoId}")
    int updateOne(@Param("username") String username, @Param("cartInfoId") Long cartInfoId, @Param("status") Short status);
}
