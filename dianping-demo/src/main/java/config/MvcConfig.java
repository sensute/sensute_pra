package config;


import inercepter.LoginInterceptor;
import inercepter.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * mvc配置
 * 这段 Java 代码定义了一个 Spring MVC 的配置类 MvcConfig，
 * 它实现了 WebMvcConfigurer 接口。该类主要用于配置 Spring MVC 的拦截器，
 * 包含了登录拦截器和 Token 续命拦截器，
 * 并且通过 @Resource 注解注入了一个 StringRedisTemplate 实例，
 * 用于在拦截器中与 Redis 进行交互。
 *
 * @author shensut
 *
 */
@Configuration

//WebMvcConfigurer 是 Spring MVC 提供的一个接口，通过实现该接口，
//可以对 Spring MVC 进行自定义配置，例如添加拦截器、格式化器、视图解析器等。
public class MvcConfig implements WebMvcConfigurer {

    //StringRedisTemplate：是 Spring Data Redis 提供的一个模板类，专门用于处理字符串类型的 Redis 操作
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //登陆拦截器
        registry
                .addInterceptor(new LoginInterceptor())
                //excludePathPatterns：指定不需要进行登录验证的请求路径。
                //例如，/user/code、/user/login 等路径通常是用户获取验证码和登录的接口，不需要进行登录验证
                .excludePathPatterns("/user/code"
                        , "/user/login"
                        , "/blog/hot"
                        , "/shop/**"
                        , "/shop-type/**"
                        , "/upload/**"
                        , "/voucher/**"
                )
                .order(1);
        //Token续命拦截器
        registry
                .addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**")
                .order(0);
    }
}
