package com.wjx.user_center_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wjx.user_center_backend.common.ErrorCode;
import com.wjx.user_center_backend.exception.BusinessException;
import com.wjx.user_center_backend.mapper.UserMapper;
import com.wjx.user_center_backend.model.domain.User;
import com.wjx.user_center_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;

import static com.wjx.user_center_backend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author wjx
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    private final Gson gson = new Gson();

    //盐值
    private static final String SALT = "wjx";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号过长");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        // 账户不能包含特殊字符
        String specialCharPattern = "[^a-zA-Z0-9_]";
        if (userAccount.matches(".*" + specialCharPattern + ".*")) {
            return -1;
        }
        // 密码和校验密码必须相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            return -1;
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());


        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();

    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String specialCharPattern = "[^a-zA-Z0-9_]";
        if (userAccount.matches(".*" + specialCharPattern + ".*")) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            log.info("login fail userAccount can not match password");
            return null;
        }

        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);

        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originalUser
     * @return
     */
    @Override
    public User getSafetyUser(User originalUser) {
        if (originalUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originalUser.getId());
        safetyUser.setUsername(originalUser.getUsername());
        safetyUser.setUserAccount(originalUser.getUserAccount());
        safetyUser.setAvatarUrl(originalUser.getAvatarUrl());
        safetyUser.setGender(originalUser.getGender());
        safetyUser.setPhone(originalUser.getPhone());
        safetyUser.setEmail(originalUser.getEmail());
        safetyUser.setPlanetCode(originalUser.getPlanetCode());
        safetyUser.setUserStatus(originalUser.getUserStatus());
        safetyUser.setCreateTime(originalUser.getCreateTime());
        safetyUser.setUserRole(originalUser.getUserRole());
        safetyUser.setTags(originalUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 更新用户
     * @param user
     * @return
     */
    @Override
    public boolean updateUser(User user) {
        return this.updateById(user);
    }

    @Override
    public List<User> selectSearch(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 用户名：模糊查询
        if (user.getUsername() != null) {
            queryWrapper.like("username", user.getUsername());
        }
        // 用户账户：模糊查询
        if (user.getUserAccount() != null) {
            queryWrapper.like("userAccount", user.getUserAccount());
        }
        // 性别：精确查询
        if (user.getGender() != null) {
            queryWrapper.eq("gender", user.getGender());
        }
        // 电话：模糊查询
        if (user.getPhone() != null) {
            queryWrapper.like("phone", user.getPhone());
        }
        // 邮箱：模糊查询
        if (user.getEmail() != null) {
            queryWrapper.like("email", user.getEmail());
        }
        // 状态：精确查询
        if (user.getUserStatus() != null) {
            queryWrapper.eq("userStatus", user.getUserStatus());
        }
        // 星球编号：精确查询
        if (user.getPlanetCode() != null ) {
            queryWrapper.eq("planetCode", user.getPlanetCode());
        }
        // 角色：精确查询
        if (user.getUserRole() != null) {
            queryWrapper.eq("userRole", user.getUserRole());
        }
        // 标签：模糊查询
        if (user.getTags() != null &&!user.getTags().isEmpty()) {
            List<String> tagList = gson.fromJson(user.getTags(), new TypeToken<List<String>>() {}.getType());
            queryWrapper.in("tags", tagList);
        }

        return userMapper.selectList(queryWrapper);

    }

}




