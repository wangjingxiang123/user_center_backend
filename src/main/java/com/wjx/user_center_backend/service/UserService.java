package com.wjx.user_center_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wjx.user_center_backend.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 14996
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-02-28 16:49:36
 */
public interface UserService extends IService<User> {



    /**
     * 用户注释
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originalUser
     * @return
     */
    User getSafetyUser(User originalUser);

    /**
     * 用户注销
     */
    int userLogout(HttpServletRequest request);

    /**
     * 更新用户
     * @param user
     * @return
     */
    boolean updateUser(User user);


    /**
     * 根据条件查询用户
     * @param user
     */
    List<User> selectSearch(User user);
}
