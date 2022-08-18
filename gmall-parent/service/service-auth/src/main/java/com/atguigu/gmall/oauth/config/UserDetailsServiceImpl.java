package com.atguigu.gmall.oauth.config;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.oauth.mapper.UserInfoMapper;
import com.atguigu.gmall.oauth.util.UserJwt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 自定义授权认证类
 * 当前类的作用主要是有两个
 *      验证client_id和client_secret，即验证用户从哪里发起登录请求的，是否合法
 *      如果是授权码登录，验证完client_id和client_secret通过即登录成果。如果是使用密码模式登录，还需要验证用户名和密码
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Autowired
    ClientDetailsService clientDetailsService;

    /**
     * 自定义授权认证
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用http basic认证，http basic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication == null){
            //通过客户端id查询客户端秘钥
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            //如果是授权码模式，用户不需要登录输入账号和密码，验证通过直接登录成功
            if(clientDetails != null){
                //获取查询到的秘钥
                String clientSecret = clientDetails.getClientSecret();
                //校验用户的客户端id和客户端秘钥是否正确
                return new User(username,
                        clientSecret,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        //密码模式---校验用户的用户名是否为空
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        //根据用户名查询用户信息
//        String pwd = new BCryptPasswordEncoder().encode("atguigu");
        //动态获取密码
        UserInfo userInfo =
                userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getLoginName, username));
        //判断用户是否存在
        if (userInfo == null) {
            return  null;
        }
        //创建User对象,校验用户名密码
        UserJwt userDetails = new UserJwt(username,
                userInfo.getPasswd(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(""));
        //返回结果
        return userDetails;
    }

//    public static void main(String[] args) {
//        String pwd = new BCryptPasswordEncoder().encode("atguigu");
//        System.out.println("pwd = " + pwd);
//    }
}
