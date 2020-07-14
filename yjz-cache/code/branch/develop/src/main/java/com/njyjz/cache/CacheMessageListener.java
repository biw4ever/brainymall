package com.njyjz.cache;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

public class CacheMessageListener implements MessageListener
{
    private final Logger logger = LogManager.getLogger(CacheMessageListener.class);
    
    private RedisTemplate<Object, Object> redisTemplate;
    
    private YjzCacheManager yjzCacheManager;
    
    public CacheMessageListener(RedisTemplate<Object, Object> redisTemplate,
        YjzCacheManager yjzCacheManager)
    {
        super();
        this.redisTemplate = redisTemplate;
        this.yjzCacheManager = yjzCacheManager;
    }
    
    @Override
    public void onMessage(Message message, byte[] pattern)
    {
        CacheMessage cacheMessage = (CacheMessage)redisTemplate.getValueSerializer().deserialize(message.getBody());
        logger.debug("recevice a redis topic message, clear local cache, the cacheName is {}, the key is {}",
            cacheMessage.getSrcIp(),
            cacheMessage.getCacheName(),
            cacheMessage.getKey());
        
        // 请求IP为空，不清理本地缓存
        // 本地IP查不出来，不清理本地缓存
        // 仅当请求IP和本地IP不相同，才清理本地缓存，以便下次访问缓存时从数据源更新
        String localIpAddress = YjzCache.getLocalIpAddress();
        if(cacheMessage.getSrcIp() != null && localIpAddress != null && !localIpAddress.equals(cacheMessage.getSrcIp()))
        {
            yjzCacheManager.clearLocal(cacheMessage.getCacheName(), cacheMessage.getKey());
        }
    }
}
