package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;//会自动扫描名字为类型第一个字母小写所对应的bean

    /**
     * 用户登陆
     *
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> login(String username, String password, HttpSession session) {
        ServiceResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    @RequestMapping(value = "loginout.do", method =RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> loginout(String username, String password, HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);

        return ServiceResponse.createBySuccess();
    }

    @RequestMapping(value = "register.do", method =RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> register(User user) {
        return iUserService.register(user);
    }

    @RequestMapping(value = "check_valid.do", method =RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> checkValid(String str, String type) {
        return iUserService.checkVaild(str, type);
    }

    @RequestMapping(value = "get_user_info.do", method =RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null)
            return ServiceResponse.createBySuccess(user);
        return ServiceResponse.createByErrorMessage("用户未登陆，无法获取当前用户的信息");
    }

    /**
     * 获取问题
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do", method =RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    /**
     * 校核问题答案
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do", method =RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> forgetCheckQuestion(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }


    /**
     * 因为在确定了密码保护的问题正确的话，其开始设置新的密码。且返回给界面的为token
     * 因为设置密码的时候是有限制的，所以先才缓存当中读取相应的username对应的token，如果存在则可以进行密码的设置，否则实际那过期了，不可以进行密码的重新设置
     *
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do", method =RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken) {
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    /**
     * 登陆状态下的重置密码
     */
    @RequestMapping(value = "reset_password.do", method =RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> restPassword(HttpSession session, String passwordOld, String passwordNew) {
        User resultCount = (User) session.getAttribute(Const.CURRENT_USER);
        if (resultCount == null)
            return ServiceResponse.createByErrorMessage("不是在登陆状态");
        return iUserService.restPassword(passwordOld, passwordNew, resultCount);
    }

    /**
     * 用户信息的更新
     */
    @RequestMapping(value = "update_information.do", method =RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> update_information(HttpSession session, User user) {

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null)
            return ServiceResponse.createByErrorMessage("用户未登录");
        /**
         * 因为传过来的user信息是没有userid的，所以先从session中 得到userid
         */
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServiceResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess())
            session.setAttribute(Const.CURRENT_USER, response.getData());
        return response;
    }
    @RequestMapping(value = "get_information.do", method =RequestMethod.POST)
    @ResponseBody

    public ServiceResponse<User> get_information(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要进行登陆");
        /**
         * 需要将密码变为空
         */
        return iUserService.getInformation(user.getId());
    }



}
