package com.njyjz.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@EnableConfigurationProperties(YjzCacheProperties.class)
public class YjzCacheAutoConfiguration
{
    @Autowired
    private YjzCacheProperties yjzCacheProperties;
    
    @Bean
//    @ConditionalOnBean(RedisTemplate.class)
    public YjzCacheManager yjzCacheManager(RedisTemplate<Object, Object> redisTemplate)
    {
        return new YjzCacheManager(yjzCacheProperties, redisTemplate);
    }
    
    @Bean
    public RedisMessageListenerContainer yjzCacheListenerContainer(RedisTemplate<Object, Object> redisTemplate,
        YjzCacheManager redisCaffeineCacheManager)
    {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisTemplate.getConnectionFactory());
        CacheMessageListener cacheMessageListener = new CacheMessageListener(redisTemplate, redisCaffeineCacheManager);
        redisMessageListenerContainer.addMessageListener(cacheMessageListener,
            new ChannelTopic(yjzCacheProperties.getRedisTopic()));
        return redisMessageListenerContainer;
    }
}
