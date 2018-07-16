package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.vo.CartVo;

/**
 * @author Jorker
 * @date 2018/6/28 17:59
 */
public interface ICartService {
    ServiceResponse<CartVo> add(Integer userId, Integer productId, Integer productNum);
    ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer productNum);
    ServiceResponse<CartVo> delete(Integer userId, String productIds);
    ServiceResponse<CartVo> list(Integer userId);
    ServiceResponse<CartVo> selectOrUnselect(Integer userId, Integer checked,Integer productId);
    ServiceResponse<Integer> getCartProductCount(Integer userId);
}
