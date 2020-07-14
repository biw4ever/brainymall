package com.njyjz.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.njyjz.cache.YjzCacheProperties.CacheType;

public class YjzCacheManager implements CacheManager
{
    
    private final Logger logger = LogManager.getLogger(YjzCacheManager.class);
    
    private ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();
    
    private YjzCacheProperties cacheRedisCaffeineProperties;
    
    private RedisTemplate<Object, Object> redisTemplate;
    
    public YjzCacheManager(YjzCacheProperties cacheRedisCaffeineProperties,
        RedisTemplate<Object, Object> redisTemplate)
    {
        super();
        this.cacheRedisCaffeineProperties = cacheRedisCaffeineProperties;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public Cache getCache(String name)
    {
        Cache cache = cacheMap.get(name);
        if (cache != null)
        {
            return cache;
        }

        cache = new YjzCache(name, redisTemplate, caffeineCache(name), cacheRedisCaffeineProperties);
        Cache oldCache = cacheMap.putIfAbsent(name, cache);
        logger.debug("create cache instance, the cache name is : {}", name);
        return oldCache == null ? cache : oldCache;
    }
    
    public com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache(String name)
    {
        Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
        CacheType cacheType = cacheRedisCaffeineProperties.getTypes().get(name);
        
        if (cacheType.getCaffeine().getExpireAfterAccess() > 0)
        {
            cacheBuilder.expireAfterAccess(cacheType.getCaffeine().getExpireAfterAccess(),
                TimeUnit.MILLISECONDS);
        }
        if (cacheType.getCaffeine().getExpireAfterWrite() > 0)
        {
            cacheBuilder.expireAfterWrite(cacheType.getCaffeine().getExpireAfterWrite(),
                TimeUnit.MILLISECONDS);
        }
        if (cacheType.getCaffeine().getInitialCapacity() > 0)
        {
            cacheBuilder.initialCapacity(cacheType.getCaffeine().getInitialCapacity());
        }
        if (cacheType.getCaffeine().getMaximumSize() > 0)
        {
            cacheBuilder.maximumSize(cacheType.getCaffeine().getMaximumSize());
        }
        if (cacheType.getCaffeine().getRefreshAfterWrite() > 0)
        {
            cacheBuilder.refreshAfterWrite(cacheType.getCaffeine().getRefreshAfterWrite(),
                TimeUnit.MILLISECONDS);
        }
        return cacheBuilder.build();
    }
    
    @Override
    public Collection<String> getCacheNames()
    {
        List<String> cacheNames = new ArrayList<>();
        for(Entry<String, CacheType> entry : cacheRedisCaffeineProperties.getTypes().entrySet())
        {
            cacheNames.add(entry.getKey());
        }
        return cacheNames;
    }
    
    public void clearLocal(String cacheName, Object key)
    {
        Cache cache = cacheMap.get(cacheName);
        if (cache == null)
        {
            return;
        }
        YjzCache redisCaffeineCache = (YjzCache)cache;
        redisCaffeineCache.clearLocal(key);
    }
}
