package com.atguigu.gmall.oauth.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.oauth.service.LoginService;
import com.atguigu.gmall.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

/**
 * @author: rlk
 * @date: 2022/8/8
 * Description: 登录功能的控制层
 */
@RestController
@RequestMapping("/user/login")
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 登录方法，用户传入username和password，在此方法拼接上grant_type，Authorization请求头，
     * 简化前端登录需要传递的参数
     * @param username
     * @param password
     * @return
     */
    @PostMapping
    public Result login(String username, String password, HttpServletRequest request) {
        AuthToken authToken = loginService.login(username, password);

        //为了防止token被盗用，需要将token放到redis中和IP绑定，这里没有设置超时时间！
        stringRedisTemplate.opsForValue()
                .set(IpUtil.getIpAddress(request), authToken.getAccessToken());

        return Result.ok(authToken);
    }


    /**
     * 测试解码token的头和载荷
     * @param args
     */
//    public static void main(String[] args) {
//        //token的头部数据
//        String tokenHeadSecret = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9";
//        byte[] tokenHead = Base64.getDecoder().decode(tokenHeadSecret.getBytes());
//        System.out.println("tokenHead = " + new String(tokenHead));
//
//        //token的载荷数据
//        String tokenPayloadSecret = "eyJzY29wZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MjA5MTk0NzgwMCwianRpIjoiYTZkMzMwNWYtMzM0OC00OGI4LTlmMjUtYjQ4ZDgyMjVjMmU5IiwiY2xpZW50X2lkIjoiYmFpZHUiLCJ1c2VybmFtZSI6InJsayJ9";
//        byte[] tokenPayload = Base64.getDecoder().decode(tokenPayloadSecret.getBytes());
//        System.out.println("tokenPayload = " + new String(tokenPayload));
//    }
}
