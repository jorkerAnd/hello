package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
/*
保证序列化json对象的时候，key为null的时候不返回json对象
 */
public class ServiceResponse<T> implements Serializable {

    private int status;
    private String msg;
    private T data;

    private ServiceResponse(int status) {
        this.status = status;
    }

    private ServiceResponse(int status, T date) {
        this.status = status;
        this.data = date;
    }

    private ServiceResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    private ServiceResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    /**
     * 因为private的构造器只能再本类中进行调用，所以在本类声明一个对外的public的类方法     * @param <T>
     *
     * @return
     */
    /**
     * 创建成功，并且返回一个message信息
     *
     * @param msg
     * @param <T>
     * @return
     */


    /**
     * 创建成功，并且将data放进去,并且避免了想要传入的参数为String类型时，不知道该调用何种构造方法的情况发生
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ServiceResponse<T> createBySuccess(T data) {
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> ServiceResponse<T> createBySuccess(String msg, T data) {
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> ServiceResponse<T> createBySuccessMessage(String msg) {
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }
    public static <T> ServiceResponse<T> createBySuccess() {
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode());
    }




    public static <T> ServiceResponse<T> createByError() {
        return new ServiceResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getMsg());
    }


    public static <T> ServiceResponse<T> createByErrorMessage(String msg) {
        return new ServiceResponse<T>(ResponseCode.ERROR.getCode(), msg);
    }

    public static <T> ServiceResponse<T> createByCodeMessage(int errorCode, String errorMessage) {
        return new ServiceResponse<T>(errorCode, errorMessage);
    }


    /**
     * 注意类方法和方法返回类型带有泛型时的书写方法
     * @return
     */
    public ServiceResponse<T> a(){
        return null;
    }
}

