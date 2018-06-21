package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.common.TokenCahe;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.utils.MD5Utils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServiceResponse<User> login(String username, String password) {
        int resultcount = userMapper.checkUsername(username);
        if (resultcount == 0) {
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }
        // 密码登陆MD5
        //因为数据库中的都是MD5加密的，所以需先将其进行翻译成加密后的处理
        password = MD5Utils.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, password);
        if (user == null) {
            return ServiceResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccess("登陆成功", user);

    }

    /*
    因为
     */
    @Override
    public ServiceResponse<String> register(User user) {
        ServiceResponse validReponse = this.checkVaild(user.getUsername(), Const.USERNAME);
        if (!validReponse.isSuccess()) {
            //不为空
            return validReponse;
        }
        validReponse = this.checkVaild(user.getEmail(), Const.EMAIL);
        if (!validReponse.isSuccess()) {
            return validReponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Utils.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("注册失败");
        }
        return ServiceResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 登陆时的表单验证
     *
     * @param str
     * @param type
     * @return
     */

    public ServiceResponse<String> checkVaild(String str, String type) {
/**
 * 对于Stringutils的isEmpty即使是” “双引号中间有空的字符串也可以为true
 * 而isblank即使是中间有空白的字符串也是会返回false
 */
        if (!StringUtils.isBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0)
                    return ServiceResponse.createByErrorMessage("用户名已存在");
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0)
                    return ServiceResponse.createByErrorMessage("email已存在");
            }
        } else {
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        return ServiceResponse.createBySuccessMessage("校验成功");


    }


    public ServiceResponse<String> selectQuestion(String username) {
        ServiceResponse validResponse = this.checkVaild(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            //用户不存在
            return ServiceResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByErrorMessage("该用户的密码问题为空");

    }

    public ServiceResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            //说明问题和用户的问题是相同的
            String forget = UUID.randomUUID().toString();
            TokenCahe.setKey(TokenCahe.TOKEN_PREFIX + username, forget);
            return ServiceResponse.createBySuccess(forget);
        }
        return ServiceResponse.createByErrorMessage("问题的答案错误");
    }

    /**
     * 修改密码必须有username和token两个节点，缺一不行，这是为了安全性进行考虑
     *
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public ServiceResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        /**
         * 虽然说username已经在得到问题的之前得到了验证，但是为了安全性还是要再次进行username是否存在的验证
         */
        if (StringUtils.isBlank(forgetToken)) {
            return ServiceResponse.createByErrorMessage("参数错误，token需要传送");
        }
        ServiceResponse validResponse = this.checkVaild(username, Const.USERNAME);


/**
 * 先判断是否为空，再去判断是否其不为空的时候是否存在于数据库当中
 */
        if (StringUtils.isBlank(username) || validResponse.isSuccess()) {
            //用户不存在
            return ServiceResponse.createByErrorMessage("用户不存在");
        }

        String token = TokenCahe.getKey(TokenCahe.TOKEN_PREFIX + username);
/**
 * isBlank的范围比isEmpty的范围大，包括null的情况
 */
        if (StringUtils.isBlank(token)) {
            return ServiceResponse.createByErrorMessage("token无效或者已经过期");
        }

        if (StringUtils.equals(token, forgetToken)) {
            String md5Password = MD5Utils.MD5EncodeUtf8(passwordNew);
            //通过username来修改密码
            int rowcount = userMapper.updatePasswordByUsername(username, md5Password);
            if (rowcount > 0)
                return ServiceResponse.createBySuccessMessage("修改密码成功");
        } else {
            return ServiceResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }

        return ServiceResponse.createByErrorMessage("修改密码失败");
    }

    public ServiceResponse<String> restPassword(String passwordOld, String passwordNew, User user) {
//先从session中拿出来user，然后校验旧密码是否和和userid对应的密码相同，然后再进行更新
        int result = userMapper.checkPassword(MD5Utils.MD5EncodeUtf8(passwordOld), user.getId());

        if (result == 0) {
            return ServiceResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Utils.MD5EncodeUtf8(passwordNew));
        int resultCount = userMapper.updateByPrimaryKeySelective(user);
        if (resultCount > 0)
            return ServiceResponse.createBySuccessMessage("更新成功");
        return ServiceResponse.createByErrorMessage("密码更新失败");
    }


    public ServiceResponse<User> updateInformation(User user) {
        //username是不能被更新
        //email也要进行一个校验，校验新的Email是不是已经存在，并且存在的Email如果相同的话，不能是我们当前的这个用户
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0)
            return ServiceResponse.createByErrorMessage("email已经绑定了其他用户");
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUsername(user.getUsername());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0)
            return ServiceResponse.createBySuccess("更新成功", updateUser);
        return ServiceResponse.createByErrorMessage("更新失败");
    }

    public ServiceResponse<User> getInformation(Integer userid) {
        User user = userMapper.selectByPrimaryKey(userid);
        if (user == null) {
            return ServiceResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccess(user);

    }

    //backend后台管理

    /**
     * 校验是否为管理员
     * @param user
     * @return
     */
    public ServiceResponse checkAdminRole(User user) {
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN)
            return ServiceResponse.createBySuccess();
        return ServiceResponse.createByError();
    }


}
