package com.mmall.common;

import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
    }

    public interface Cart {
        int CHECKED = 1;//即购物车为选中状态
        int UN_CHECKED = 0;//购物车不是选中的状态
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public interface Role {//声明一个抽象类用来减少成本
        int ROLE_CUSTOMER = 0;//普通用户
        int ROLE_ADMIN = 1;//管理用户
    }

    /**
     * 注意声明类的时候，不要加（）
     */
    public enum productStatusEnum {
        ON_SALE(1, "在线状态");
        private String value;
        private int code;

        productStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }


    public enum OrderStatusEnnum {
        CANCEL("已取消", 0),
        NO_PAY("未支付", 10),
        PAID("已付款", 20),
        SHIPPED("已发货", 40),
        ORDER_SUCCESS("订单完成", 50),
        ORDER_CLOSE("订单关闭", 60);
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        OrderStatusEnnum(String value, int code) {
            this.value = value;
            this.code = code;

        }

        public static OrderStatusEnnum codeof(int code) {
            for (OrderStatusEnnum orderStatusEnnum : values()) {
                if (orderStatusEnnum.getCode() == code)
                    return orderStatusEnnum;
            }
            throw new RuntimeException("没有找到对应的枚举类");

        }
    }

    public interface AlipayCallback {
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PayPlatfromEnum {
        ALIPAY("支付宝", 1);


        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        PayPlatfromEnum(String value, int code) {
            this.value = value;
            this.code = code;

        }


    }

    public enum PaymentTypeEnum {
        ON_LINE_PAY("在线支付", 1);
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        PaymentTypeEnum(String value, int code) {
            this.value = value;
            this.code = code;

        }

        public static PaymentTypeEnum codeof(int code) {
            for (PaymentTypeEnum paymentTypeEnum : values()) {
                if (paymentTypeEnum.getCode() == code)
                    return paymentTypeEnum;
            }
            throw new RuntimeException("没有找到对应的枚举类");

        }

    }
}
