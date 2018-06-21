package com.mmall.common;

public class Const {
    public static final String CURRENT_USER="currentUser";
    public static final String EMAIL="email";
    public static final String USERNAME="username";
    public interface Role{//声明一个抽象类用来减少成本
        int ROLE_CUSTOMER=0;//普通用户
        int ROLE_ADMIN=1;//管理用户
    }
}
