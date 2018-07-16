package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Shipping;

/**
 * @author Jorker
 * @date 2018/6/30 10:40
 */
public interface IShippingService {
    ServiceResponse add(Integer userId, Shipping shipping);
    ServiceResponse<String> delete(Integer userId, Integer shippingId);
    ServiceResponse update(Integer userId, Shipping shipping);
    ServiceResponse<Shipping> select(Integer userId, Integer shippingId);
    ServiceResponse<PageInfo> list(Integer pageNum, Integer pageSize, Integer userId);
}
