package com.njyjz.cache;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import com.github.benmanes.caffeine.cache.Cache;

public class YjzCache extends AbstractValueAdaptingCache
{
    
    private final Logger logger = LogManager.getLogger(YjzCache.class);
    
    private String name;
    
    private RedisTemplate<Object, Object> redisTemplate;
    
    private Cache<Object, Object> caffeineCache;
    
    private String redisKeyPrefix;
    
    private long expiration = 0;
    
    private String topic = "yjzcache:topic";
    
    private int levels = 1;
    
    private static String localIpAddress = null;
    
    protected YjzCache(boolean allowNullValues)
    {
        super(allowNullValues);
    }
    
    public YjzCache(String name, RedisTemplate<Object, Object> redisTemplate,
        Cache<Object, Object> caffeineCache, YjzCacheProperties cacheRedisCaffeineProperties)
    {
        super(cacheRedisCaffeineProperties.getTypes().get(name).isCacheNullValues());
        this.name = name;
        this.redisTemplate = redisTemplate;
        this.caffeineCache = caffeineCache;
        this.redisKeyPrefix = cacheRedisCaffeineProperties.getTypes().get(name).getRedis().getKeyPrefix();
        this.expiration = cacheRedisCaffeineProperties.getTypes().get(name).getRedis().getExpiration();
        this.topic = cacheRedisCaffeineProperties.getRedisTopic();
        this.levels = cacheRedisCaffeineProperties.getTypes().get(name).getLevels();
    }
    
    @Override
    public String getName()
    {
        return this.name;
    }
    
    @Override
    public Object getNativeCache()
    {
        return this;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader)
    {
        Object value = lookup(key);
        if (value != null)
        {
            return (T)value;
        }
        ReentrantLock lock = new ReentrantLock();
        try
        {
            lock.lock();
            value = lookup(key);
            if (value != null)
            {
                return (T)value;
            }
            value = valueLoader.call();
            Object storeValue = toStoreValue(valueLoader.call());
            put(key, storeValue);
            return (T)value;
        }
        catch (Exception e)
        {
            try
            {
                Class<?> c = Class.forName("org.springframework.cache.Cache$ValueRetrievalException");
                Constructor<?> constructor = c.getConstructor(Object.class, Callable.class, Throwable.class);
                RuntimeException exception = (RuntimeException)constructor.newInstance(key, valueLoader, e.getCause());
                throw exception;
            }
            catch (Exception e1)
            {
                throw new IllegalStateException(e1);
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    
    @Override
    public void put(Object key, Object value)
    {
        if (!super.isAllowNullValues() && value == null)
        {
            this.evict(key);
            return;
        }
        
        push(new CacheMessage(getLocalIpAddress(), this.name, key));
        caffeineCache.put(key, value);
        
        if (levels == 2)
        {
            Object cacheRedisKey = getRedisKey(key);
            putRedis(cacheRedisKey, value);
        }
        
    }
    
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value)
    {
        
        ValueWrapper retValue = null;
        
        PutIfAbsentFunction callable = new PutIfAbsentFunction(value);
        Object result = this.caffeineCache.get(key, callable);
        retValue = (callable.called ? null : toValueWrapper(result));
        
        if (callable.called) // 本地缓存不存在，则需要通知其他本地缓存，并且直接更新Redis缓存（不管是否Absent）
        {
            push(new CacheMessage(getLocalIpAddress(), this.name, key));
            
            if (levels == 2)
            {
                Object cacheRedisKey = getRedisKey(key);
                putRedis(cacheRedisKey, value);
            }
            
        }
        else
        {
            // 本地缓存存在，则啥也不做。
        }
        
        return retValue;
    }
    
    private void putRedis(Object cacheRedisKey, Object value)
    {
        
        long expire = getExpire();
        if (expire > 0)
        {
            redisTemplate.opsForValue().set(cacheRedisKey, toStoreValue(value), expire, TimeUnit.MILLISECONDS);
        }
        else
        {
            redisTemplate.opsForValue().set(cacheRedisKey, toStoreValue(value));
        }
    }
    
    @Override
    public void evict(Object key)
    {
        // 先清除redis中缓存数据，然后清除caffeine中的缓存，避免短时间内如果先清除caffeine缓存后其他请求会再从redis里加载到caffeine中
        if (levels == 2)
        {
            redisTemplate.delete(getRedisKey(key));
        }
        
        push(new CacheMessage(getLocalIpAddress(), this.name, key));
        caffeineCache.invalidate(key);
    }
    
    @Override
    public void clear()
    { //
      // 先清除redis中缓存数据，然后清除caffeine中的缓存，避免短时间内如果先清除caffeine缓存后其他请求会再从redis里加载到caffeine中
        if (levels == 2)
        {
            Set<Object> keys = redisTemplate.keys(this.name.concat(":"));
            for (Object key : keys)
            {
                redisTemplate.delete(key);
            }
        }
        
        push(new CacheMessage(getLocalIpAddress(), this.name, null));
        caffeineCache.invalidateAll();
    }
    
    @Override
    protected Object lookup(Object key)
    {
        
        Object value = caffeineCache.getIfPresent(key);
        if (value != null)
        {
            logger.debug("get cache from caffeine, the key is : {}", key);
            return value;
        }
        
        Object redisKey = getRedisKey(key);
        value = redisTemplate.opsForValue().get(redisKey);
        if (value != null)
        {
            logger.debug("get cache from redis and put in caffeine, the key is : {}", redisKey);
            caffeineCache.put(key, value);
        }
        return value;
    }
    
    private Object getRedisKey(Object key)
    {
        return this.name.concat(":").concat(
            StringUtils.isEmpty(redisKeyPrefix) ? key.toString() : redisKeyPrefix.concat(":").concat(key.toString()));
    }
    
    /**
     * 如果没有配置过期时间，设置15分钟过期时间
     * 
     * @return
     */
    private long getExpire()
    {
        return expiration == 0 ? 9000000 : expiration;
    }
    
    /**
     * * @description 缓存变更时通知其他节点清理本地缓存
     * 
     * message
     */
    private void push(CacheMessage message)
    {
        redisTemplate.convertAndSend(topic, message);
    }
    
    /**
     * * @description 清理本地缓存 * 
     */
    public void clearLocal(Object key)
    {
        logger.debug("clear local cache, the key is : {}", key);
        if (key == null)
        {
            caffeineCache.invalidateAll();
        }
        else
        {
            caffeineCache.invalidate(key);
        }
    }
    
    private class PutIfAbsentFunction implements Function<Object, Object>
    {
        
        private final Object value;
        
        private boolean called;
        
        public PutIfAbsentFunction(Object value)
        {
            this.value = value;
        }
        
        @Override
        public Object apply(Object key)
        {
            this.called = true;
            return toStoreValue(this.value);
        }
    }
    
    private class LoadFunction implements Function<Object, Object>
    {
        
        private final Callable<?> valueLoader;
        
        public LoadFunction(Callable<?> valueLoader)
        {
            this.valueLoader = valueLoader;
        }
        
        @Override
        public Object apply(Object o)
        {
            try
            {
                return toStoreValue(valueLoader.call());
            }
            catch (Exception ex)
            {
                throw new ValueRetrievalException(o, valueLoader, ex);
            }
        }
    }
    
    public static String getLocalIpAddress()
    {
        if (localIpAddress != null)
        {
            return localIpAddress;
        }
        
        InetAddress inet = null;
        try
        {
            inet = InetAddress.getLocalHost();
            localIpAddress = inet.getHostAddress();
            return inet.getHostAddress();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return null;
        }
 
    }
}