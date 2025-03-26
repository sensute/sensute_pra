package config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson配置
 * Redisson 是一个基于 Redis 实现的 Java 驻内存数据网格（In-Memory Data Grid），
 * 它提供了分布式和可扩展的 Java 数据结构，如分布式锁、分布式集合等。通过该配置类，
 * 我们可以根据 Spring 配置文件中的 Redis 连接信息，创建一个 RedissonClient 实例，
 * 以便在项目中方便地使用 Redisson 提供的各种功能。
 *
 * @author shensut
 */

@Configuration
public class RedissonConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private String port;
    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedissonClient redissonClient(){
        //配置
        Config config=new Config();
        config.useSingleServer().setAddress("redis://"+host+":"+port).setPassword(password);
        //创建对并且返回
        return Redisson.create(config);
    }

}
