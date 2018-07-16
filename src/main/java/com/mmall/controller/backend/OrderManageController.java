package com.mmall.controller.backend;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author Jorker
 * @date 2018/7/4 11:19
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse<PageInfo> orderList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            return iOrderService.manageList(pageNum, pageSize);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }


    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse detail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            return iOrderService.manageDetail(orderNo);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }


    @RequestMapping("search.do")
    @ResponseBody
    public ServiceResponse<PageInfo> orderSearch(HttpSession session, Long orderNo, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            return iOrderService.manageSearch(orderNo, pageNum, pageSize);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }

    /**
     * 发货成功
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServiceResponse<String> orderSearch(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            return iOrderService.manageSendGoods(orderNo);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }





}
