package fun.luomo.kill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @Classname RedssonConfig
 * @Description TODO
 * @Author Jack
 * Date 2020/9/16 20:47
 * Version 1.0
 */
@Configuration
public class RedissonConfig {

    @Bean(name = "redissonClient",destroyMethod = "shutdown")
    public RedissonClient redissonClient() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.216.128:6379");
        config.useSingleServer().setPassword("zxcasdqwe1234567890");
        config.useSingleServer().setConnectionPoolSize(1000);
        config.useSingleServer().setConnectionMinimumIdleSize(100);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
