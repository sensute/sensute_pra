package utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * redis ID生成器
 * 该 ID 由两部分组成：时间戳部分和序列号部分。
 * 通过将这两部分进行拼接，确保生成的 ID 在分布式系统中具有唯一性，
 * 并且可以保证生成的 ID 是趋势递增的，适用于需要唯一标识的业务场景，
 * 如订单号、用户 ID 等。
 *
 * @author shensut
 */
@Component
public class RedisWorker {
    /**
     * 初始时间戳
     */
    private static final Long BEGIN_TIMESTAMP = 1640995200L;
    /**
     * 序列号位数
     */
    private static final Integer COUNT_BITS = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 获取id
     *
     * @param keyPrefix 业务前缀
     * @return {@link Long}
     */
    public Long nextId(String keyPrefix) {
        //生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        //生成序列号
        //生成当前日期 精确到天
        String today = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        //自增长
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + today);
        //拼接并返回
        return timestamp << COUNT_BITS|count ;
    }
}
