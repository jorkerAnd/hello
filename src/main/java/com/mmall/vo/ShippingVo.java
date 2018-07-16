package com.mmall.vo;

import lombok.Data;

/**
 * @author Jorker
 * @date 2018/7/3 19:48
 */
@Data
public class ShippingVo {

    private Integer userId;

    private String receiverName;

    private String receiverPhone;

    private String receiverMobile;

    private String receiverProvince;

    private String receiverCity;

    private String receiverDistrict;

    private String receiverAddress;

    private String receiverZip;

}
