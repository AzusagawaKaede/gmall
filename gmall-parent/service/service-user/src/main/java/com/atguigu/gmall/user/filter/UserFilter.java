package com.atguigu.gmall.user.filter;

import com.atguigu.gmall.user.utils.ThreadLocalUtil;
import com.atguigu.gmall.user.utils.TokenUtil;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/9
 * Description: 自定义过滤器，解析token获取username到ThreadLocal中
 */
@Order(1)
@WebFilter(filterName = "userFilter", urlPatterns = "/*")
public class UserFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //获取token
        HttpServletRequest req = (HttpServletRequest) request;
        String token =
                req.getHeader("Authorization").replace("bearer ", "");

        //解析token
        Map<String, String> tokenMap = TokenUtil.dcodeToken(token);
        if (tokenMap != null || tokenMap.size() > 0) {
            //获取username
            String username = tokenMap.get("username");

            //保存到ThreadLocal -- 创建ThreadLocal工具类
            ThreadLocalUtil.set(username);
        }

        //放行
        chain.doFilter(request, response);
    }
}
