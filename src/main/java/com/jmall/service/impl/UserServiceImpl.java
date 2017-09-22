package com.jmall.service.impl;

import com.jmall.common.Const;
import com.jmall.common.ServerResponse;
import com.jmall.common.TokenCache;
import com.jmall.dao.UserMapper;
import com.jmall.pojo.User;
import com.jmall.service.IUserService;
import com.jmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = this.userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("username does not Exist");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);

        User user = this.userMapper.selectLogin(username,md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("password does not match");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("Login Successfully", user);

    }

    public ServerResponse<String> register(User user) {

        ServerResponse validationResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if ( !validationResponse.isSuccess()) {
            return validationResponse;
        }

        validationResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if ( !validationResponse.isSuccess()) {
            return validationResponse;
        }


        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("register failed");
        }

        return ServerResponse.createBySuccess("register successfully");
    }

    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = this.userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("username already exists");
                }
            }

            if (Const.EMAIL.equals(type)) {
                int resultCount = this.userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("email already exists");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("no valid parameters");
        }

        return ServerResponse.createBySuccessMessage("validate successfully");
    }

    public ServerResponse selectQuestion(String username) {
        ServerResponse validationResponse = this.checkValid(username, Const.USERNAME);
        if (validationResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("No such Username");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccessMessage(question);
        }

        return ServerResponse.createByErrorMessage("No matching question");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("The answer is not correct");
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("parameter error, pass token please");
        }

        ServerResponse validationResponse = this.checkValid(username, Const.USERNAME);
        if (validationResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("No such Username");
        }

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("invalid token or token is expired");
        }

        if (StringUtils.equals(forgetToken,token)) {
            String md5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5PasswordNew);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("update password successfully");
            }
        } else {
            return ServerResponse.createByErrorMessage("token error, please get token again");
        }

        return ServerResponse.createByErrorMessage("fail to update password");
    }


    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        // 为了防止横向越权，一定要校验下这个用户的旧密码，一定要指定是这个用户。
        // 因为我们要查询一个count(1)出来，如果不指定id， 那么结果就是true count > 0
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("old password error");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("update password successfully");
        }

        return ServerResponse.createByErrorMessage("fail to update password");

    }

    public ServerResponse<User> updateUserInformation(User user) {
        // username cannot be updated

        // validate email
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("email exists, please use another email");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("update user info successfully", updateUser);
        }

        return ServerResponse.createByErrorMessage("fail to update user information");
    }

    public ServerResponse<User> getUserInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("cannot find current user");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }



}
