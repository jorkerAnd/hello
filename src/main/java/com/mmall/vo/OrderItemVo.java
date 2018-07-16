package com.mmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Jorker
 * @date 2018/7/3 19:44
 */@Data
public class OrderItemVo {

    private Long orderNo;

    private Integer productId;

    private String productName;

    private String productImage;

    private BigDecimal curentUnitPrice;

    private Integer quantity;

    private BigDecimal totalPrice;

    private String createTime;

}
