package com.mmall.dao;

import com.google.common.collect.Lists;
import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUserIdProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    List<Cart> selectCartByUserId(Integer userId);

    //即使userid不存在里面那么他也会出现count=0，也把认为为全选的状态
    int selectCartProductCheckedStatusByUserId(Integer userId);

    //userId一定不能为空，productIds需要进行一下判断
    int deleteByUserIdProductIds(@Param("userId") Integer userId, @Param("productIds") List<String> productIds);

    int checkOrUncheckedProduct(@Param("userId") Integer userId, @Param("checked") Integer checked,@Param("productId") Integer productId);

      int selectCatrProcductCount(Integer userId);

      List<Cart> selectCheckedCartByUserId(Integer userId);




}