package com.atguigu.gmall.cart.filter;

import com.atguigu.gmall.cart.utils.ThreadLocalUtil;
import com.atguigu.gmall.cart.utils.TokenUtil;
import org.springframework.core.Ordered;
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
 * Description: 自定义过滤器，在此过滤器中从请求里获取token并解析出username，放到ThreadLocal中
 *  ThreadLocal是线程安全的类，ThreadLocal保存的内容都保存在线程自身的ThreadLocalMap中，
 *  不会收到其他线程影响（详见ThreadLocal的set()方法）
 *
 * @Order设置过滤器的顺序，也就是优先级，值越小优先级越高
 * @WebFilter表示当前类是一个过滤器，相当于在web.xml中配置<filter>标签，因此需要传入filterName和urlPatterns
 *
 * 注意：@WebFilter注解并不是Spring官方的注解，是由Servlet提供的注解，Spring的ComponentScan扫描不到
 *          需要使用Servlet的扫描注解：@ServletComponentScan(basePackage = "")
 */
@Order(1)
@WebFilter(filterName = "cartFilter", urlPatterns = "/*")
public class CartFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //能够进入到过滤器说明请求里携带了token
        //从request中获取到token
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("Authorization").replace("bearer ", "");

        //解析token，获取username -- 使用Token解析工具类
        Map<String, String> tokenMap = TokenUtil.dcodeToken(token);
        //非空校验，校验载荷里有数据
        if (tokenMap != null && tokenMap.size() > 0) {
            String username = tokenMap.get("username");

            //将username保存到ThreadLocal中 -- 创建ThreadLocal工具类
            ThreadLocalUtil.set(username);
        }

        //放行
        chain.doFilter(request, response);
    }

}
