package com.atguigu.gmall.cart.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @author: rlk
 * @date: 2022/8/10
 * Description: Cart微服务的拦截器，拦截Feign请求，在请求中添加token相关Header
 */
@Component
public class CartFeignInterceptor implements RequestInterceptor {

    /**
     * 拦截Feign请求，在请求中添加token相关Header
     * spring mvc中，为了随时都能取到当前请求的request对象，
     * 可以通过 RequestContextHolder 的静态方法getRequestAttributes()获取Request相关的变量
     *
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        //从RequestContextHolder获取用户发送给本微服务的请求
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            //获取用户发送给本微服务的request
            HttpServletRequest request = requestAttributes.getRequest();
            //获取request中所有的Header名称
            Enumeration<String> headerNames = request.getHeaderNames();
            //遍历所有的Header名称，添加到Feign的请求中
            while (headerNames.hasMoreElements()) {
                //请求头的名字
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                template.header(headerName, headerValue);
            }
        }
    }
}
