package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.nio.cs.US_ASCII;

import javax.servlet.http.HttpSession;

/**
 * @author Jorker
 * @date 2018/6/30 10:39
 */
@Controller
@RequestMapping("shipping")
public class shippingController {

    @Autowired
    private IShippingService iShippingService;

    /**
     * 使用springmvc中的数据绑定，直接绑定一个对象
     *
     * @param httpSession
     * @param shipping
     * @return
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServiceResponse add(HttpSession httpSession, Shipping shipping) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        return iShippingService.add(user.getId(), shipping);
    }

    @RequestMapping("del.do")
    @ResponseBody
    public ServiceResponse<String> delete(HttpSession httpSession, Integer shippingId) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        return iShippingService.delete(user.getId(), shippingId);
    }

    @RequestMapping("update.do")
    @ResponseBody
    //todo 尝试用update和add可以用一个controller来进行操作
    public ServiceResponse update(HttpSession httpSession, Shipping shipping) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        return iShippingService.update(user.getId(), shipping);
    }

    @RequestMapping("select.do")
    @ResponseBody
    //todo 尝试用update和add可以用一个controller来进行操作
    public ServiceResponse<Shipping> select(HttpSession httpSession, Integer shippingId) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        return iShippingService.select(user.getId(), shippingId);
    }
    @RequestMapping("list.do")
    @ResponseBody

    public ServiceResponse<PageInfo> list(HttpSession httpSession,
                                          @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) httpSession.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMsg());
        return iShippingService.list(user.getId(), pageNum, pageSize);
    }


}
