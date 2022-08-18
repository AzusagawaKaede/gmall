package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.gateway.utils.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author: rlk
 * @date: 2022/8/8
 * Description: 全局过滤器，判断是否携带token
 */
@Component
public class GmallGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 判断是否携带token
     * token存放的三个位置：（1）Header里 （2）cookie （3）直接拼接在url上
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求对象
        ServerHttpRequest request = exchange.getRequest();

        //从url上获取
        String token = request.getQueryParams().getFirst("token");
        if (StringUtils.isEmpty(token)) {
            //说明url里没有，去Header里找
            HttpHeaders headers = request.getHeaders();
            if (headers != null && headers.size() > 0) {
                token = headers.getFirst("token");
            }
        }

        if (StringUtils.isEmpty(token)) {
            //说明cookie里也没有，从cookie里获取
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            if (cookies != null && cookies.size() > 0) {
                HttpCookie cookie = cookies.getFirst("token");
                if (cookie != null) {
                    token = cookie.getValue();
                }
            }
        }

        if (StringUtils.isEmpty(token)) {
            //说明没有token，拦截
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //判断是否有token，有则将token放到请求头
        request.mutate().header("Authorization", "bearer " + token);

        //从redis获取第一次申请token时的IP，比较IP是否一致，不一致则认为被盗用，拒绝请求
        String redisToken = stringRedisTemplate.opsForValue().get(IpUtil.getGatwayIpAddress(request));
        if (!token.equals(redisToken)) {
            //拦截
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //放行
        return chain.filter(exchange);
    }

    /**
     * 此过滤器的优先级，数值越小优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
