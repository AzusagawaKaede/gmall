package com.atguigu.gmall.oauth.service;

import com.atguigu.gmall.oauth.util.AuthToken;

/**
 * @author: rlk
 * @date: 2022/8/8
 * Description: 登录功能的业务层
 */
public interface LoginService {

    /**
     * 登录方法，用户传入username和password，在此方法拼接上grant_type，Authorization请求头，
     * 简化前端登录需要传递的参数
     * @param username
     * @param password
     * @return
     */
    AuthToken login(String username, String password);
}
