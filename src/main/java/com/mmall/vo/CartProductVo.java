package com.mmall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Jorker
 * @date 2018/6/28 18:43
 */
@Data
public class CartProductVo {
//结合产品和购物车生成的一个抽象对象
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private String productName;
    private String productSubtitle;
    private String productMainImage;
    private BigDecimal productPrice;
    private Integer productStatus;
    private BigDecimal productToalPrice;
    private Integer productStock;
    private Integer productChecked;
    private String limitQuantity;

}
