package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLDocument;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Jorker
 * @date 2018/7/2 21:06
 */
@Controller
@RequestMapping("order")

public class OrderController {
    @Autowired
    private IOrderService iOrderService;

    private static  final Logger log = LoggerFactory.getLogger(OrderController.class);
    @RequestMapping("create.do")
    @ResponseBody
    public ServiceResponse create(HttpSession httpSession, Integer shippingId) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        return iOrderService.createOrder(user.getId(),shippingId);
    }

    /**
     * 预下单的接口，将购物车中的所有商品放进orderItem的表中
     * @param httpSession
     * @return
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServiceResponse getOrderCartProduct(HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());

        return iOrderService.getOrderCartProduct(user.getId());

    }


    @RequestMapping("cancel.do")
    @ResponseBody
    public ServiceResponse cancel(HttpSession httpSession, Long orderNo) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        return iOrderService.cancel(user.getId(), orderNo);

    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse detail(HttpSession httpSession, Long orderNo) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());

        return iOrderService.getOrderDeatil(user.getId(), orderNo);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse list(HttpSession httpSession, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        return iOrderService.getOrderList(user.getId(), pageNum, pageSize);

    }


    @RequestMapping("pay.do")
    @ResponseBody
    public ServiceResponse pay(HttpSession httpSession, Long orderNo, HttpServletRequest httpServletRequest) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null || orderNo == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        String path = httpServletRequest.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(), orderNo, path);
    }

    /**
     * 成功后才会进行回调
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("alipay.do")
    @ResponseBody
    public Object aplipayCallback(HttpServletRequest httpServletRequest) {
        Map<String, String> params = Maps.newHashMap();
        Map map = httpServletRequest.getParameterMap();
        for (Iterator iterator = map.keySet().iterator(); iterator.hasNext(); ) {
            String name = (String) iterator.next();
            String[] values = (String[]) map.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        log.info("支付宝回调,sign：{}，trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());
        /**
         *remove是移除对应的键值对
         */
        params.remove("sign_type");
        try {
            boolean alipayRSAChckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8");
            if (!alipayRSAChckedV2) {
                return ServiceResponse.createByErrorMessage("非法请求");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝验证回调异常，e");

        }
        //todo 验证各种数据
        ServiceResponse serviceResponse = iOrderService.aliCallback(params);
        if (serviceResponse.isSuccess())
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        return Const.AlipayCallback.RESPONSE_FAILED;
    }


    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServiceResponse<Boolean> queryOrderPayStatus(HttpSession httpSession, Long orderNo, HttpServletRequest httpServletRequest) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null || orderNo == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        String path = httpServletRequest.getSession().getServletContext().getRealPath("upload");
        ServiceResponse serviceResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (serviceResponse.isSuccess())
            return ServiceResponse.createBySuccess(true);
        return ServiceResponse.createBySuccess(false);
    }


}
