package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Jorker
 * @date 2018/6/30 10:40
 */
@Service("iShippingService")
public class ShippingService implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServiceResponse add(Integer userId, Shipping shipping) {
        if (shipping == null)
            return ServiceResponse.createByErrorMessage("地址对象传参位空");
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0) {
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServiceResponse.createBySuccess("新建地址成功", result);
        }
        return ServiceResponse.createByErrorMessage("新建地址失败");

    }

    /**
     * 注意横向越权的问题，如果查找不和userId进行关联，可能删除的就是别的用户所对应的id
     */
    public ServiceResponse<String> delete(Integer userId, Integer shippingId) {
        if (shippingId == null)
            return ServiceResponse.createByErrorMessage("传参是空");
        //写一个和userId和shippingId绑定在一起的
        int resul = shippingMapper.deleteByUserIdAndShippingId(userId, shippingId);
        if (resul > 0)
            return ServiceResponse.createBySuccess("删除地址成功");
        return ServiceResponse.createByErrorMessage("删除地址失败");
    }

    /**
     * 查询订单详情的操作
     */
    public ServiceResponse update(Integer userId, Shipping shipping) {
        if (shipping == null)
            return ServiceResponse.createByErrorMessage("地址对象传参位空");
/**
 * 对于为什么还是需要重新重置userId，还是为了防止越权的问题出现，如果传过来的shipping中有一个别人的userId，那么将会把别人的地址更改掉
 *
 */
        shipping.setUserId(userId);
        int resultCount = shippingMapper.updateByUserIdAndShippingId(shipping);
        if (resultCount > 0)
            return ServiceResponse.createBySuccess("更新地址成功");
        return ServiceResponse.createByErrorMessage("更新地址失败");
    }

    public ServiceResponse<Shipping> select(Integer userId, Integer shippingId) {
        if (shippingId == null)
            return ServiceResponse.createByErrorMessage("地址对象传参位空");
        Shipping shipping = shippingMapper.selectByShippingIdAndUserId(userId, shippingId);
        if (shipping != null)
            return ServiceResponse.createBySuccess(shipping);
        return ServiceResponse.createByErrorMessage("查找地址查找失败");
    }


     public ServiceResponse<PageInfo> list(Integer userId,Integer pageNum,Integer pageSize){
         PageHelper.startPage(pageNum,pageSize);
         List<Shipping> shippingList=shippingMapper.selectByUserId(userId);
         PageInfo pageInfo=new PageInfo(shippingList);
         return ServiceResponse.createBySuccess(pageInfo);
     }


}
