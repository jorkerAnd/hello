package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;

public interface IUserService {
    ServiceResponse<User> login(String username, String password);

    ServiceResponse<String> register(User user);

    ServiceResponse<String> checkVaild(String str, String type);

    ServiceResponse<String> selectQuestion(String username);

    ServiceResponse<String> checkAnswer(String username, String question, String answer);

    ServiceResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);
    ServiceResponse<String> restPassword(String passwordOld, String passwordNew, User user);
    ServiceResponse<User> updateInformation(User user);
    ServiceResponse<User> getInformation(Integer userid);
    ServiceResponse checkAdminRole(User user);
}
