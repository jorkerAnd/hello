package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

/**
 * @author Jorker
 * @date 2018/7/2 21:16
 */
public interface IOrderService {
    ServiceResponse pay(Integer userId, Long orderNo, String path);
    ServiceResponse  aliCallback(Map<String, String> params);
    ServiceResponse queryOrderPayStatus(Integer userId, Long orderNo);
    ServiceResponse createOrder(Integer userId, Integer shippingId);
    ServiceResponse<String> cancel(Integer userId, Long orderNo);
    ServiceResponse getOrderCartProduct(Integer userId);
    ServiceResponse<OrderVo> getOrderDeatil(Integer userId, Long orderNo);
    ServiceResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSzie);
    ServiceResponse<PageInfo> manageList(int pageNum, int pageSize);
    ServiceResponse<OrderVo> manageDetail(Long orderNo);
    ServiceResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);
    ServiceResponse<String> manageSendGoods(Long orderNo);
}
