package com.atguigu.gmall.oauth.service.impl;

import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.oauth.service.LoginService;
import com.atguigu.gmall.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Base64;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/8
 * Description:
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;

    /**
     * 登录方法，用户传入username和password，在此方法拼接上grant_type，Authorization请求头，
     * 简化前端登录需要传递的参数， client_id和client_secret使用baidu:atguigu，在yml中配置
     * @param username
     * @param password
     * @return
     */
    @Override
    public AuthToken login(String username, String password) {
        //使用restTemplate发送POST请求给 /oauth/token
        //这里URL从负载均衡列表中获取一个，不能写死
        ServiceInstance choose = loadBalancerClient.choose("service-oauth");
        URI uri = choose.getUri();
        String url = uri + "/oauth/token";
        //构建body
        MultiValueMap<String, String> body = new HttpHeaders();
        //在body中添加参数
        body.add("username", username);
        body.add("password", password);
        body.add("grant_type", "password");
        //构建请求头
        MultiValueMap<String, String> headers = new HttpHeaders();
        String authorization = "Basic " + Base64.getEncoder().
                encodeToString((clientId + ":" + clientSecret).getBytes());
        headers.add("Authorization", authorization);
        //构建参数
        HttpEntity httpEntity = new HttpEntity(body, headers);
        ResponseEntity<Map> exchange =
                restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        //获取结果
        Map<String, String> result = exchange.getBody();

        //构建返回值
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(result.get("access_token"));
        authToken.setRefreshToken(result.get("refresh_token"));
        authToken.setJti(result.get("jti"));

        return authToken;
    }
}
