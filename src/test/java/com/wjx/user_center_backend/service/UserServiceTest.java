package com.wjx.user_center_backend.service;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wjx.user_center_backend.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAdd(){
        User user = new User();
        user.setUsername("wjx");
        user.setUserAccount("123");
        user.setAvatarUrl("https://www.codefather.cn/static/bcdh_avatar.4b4d3128.webp");
        user.setGender(0);
        user.setUserPassword("wjxwjx123*");
        user.setPhone("123");
        user.setEmail("456");
        user.setUserStatus(0);
        user.setUserRole(0);
        user.setPlanetCode("");
        boolean result = userService.save(user);
        Assertions.assertTrue(result);

    }

    @Test
    void userRegister() {
        // 测试输入为空的情况
        String userAccount = "";
        String userPassword = "12345678";
        String checkPassword = "12345678";
        String planetCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        assertEquals(-1, result);

        // 测试账户长度过短的情况
        userAccount = "abc";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        assertEquals(-1, result);

        // 测试密码长度过短的情况
        userAccount = "abcdef";
        userPassword = "1234";
        checkPassword = "1234";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        assertEquals(-1, result);

        // 测试账户包含特殊字符的情况
        userAccount = "abc@";
        userPassword = "12345678";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        assertEquals(-1, result);

        // 测试密码和校验密码不匹配的情况
        userAccount = "abcdef";
        userPassword = "12345678";
        checkPassword = "87654321";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        assertEquals(-1, result);

        // 测试正常注册情况
        userAccount = "wjx1234";
        userPassword = "12345678";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        assertEquals(1L, result);

        // 测试账户重复的情况
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        assertEquals(-1, result);
    }

}