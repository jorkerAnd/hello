package com.mmall.vo;

import com.mmall.pojo.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Jorker
 * @date 2018/6/28 18:55
 */
@Data
public class CartVo {
     private List<CartProductVo> cartProductVoList;
     private BigDecimal cartToalPrice;
     private Boolean allChecked;
     private String imageHost;
}
