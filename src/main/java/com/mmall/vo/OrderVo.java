package com.mmall.vo;

import com.mmall.pojo.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Jorker
 * @date 2018/7/3 19:36
 */
@Data
public class OrderVo {


    private Long orderNo;

    private Integer userId;

    private Integer shippingId;

    private BigDecimal payment;

    private Integer paymentType;

    private String paymentTypeDesc;

    private Integer postage;

    private Integer status;
    private String statusDesc;

    private String paymentTime;

    private String sendTime;

    private String endTime;

    private String  closeTime;

    private String  createTime;
    //订单的明细
    private List<OrderItemVo> orderItemList;
    private String imageHost;
    private String receiverName;
    private ShippingVo shippingVo;
}





